package com.vermouthx.stocker

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.vermouthx.stocker.enums.StockerMarketIndex
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.listeners.StockerQuoteReloadNotifier.*
import com.vermouthx.stocker.listeners.StockerQuoteUpdateNotifier.*
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * 股票行情应用核心类
 * 
 * 负责管理股票行情数据的定时刷新任务。
 * 每个IntelliJ项目窗口都有一个独立的StockerApp实例。
 * 
 * 核心职责：
 * 1. 调度定时任务，定期获取股票行情数据
 * 2. 通过消息总线发布数据更新事件
 * 3. 管理线程池的生命周期
 * 4. 处理刷新间隔的动态调整
 * 
 * 线程模型：
 * - 使用ScheduledExecutorService实现定时任务
 * - 单线程池，避免资源浪费
 * - 支持优雅关闭和任务取消
 * 
 * 优化特性：
 * - 合并所有市场的数据请求，避免重复
 * - 支持动态调整刷新间隔（无需重启）
 * - 线程池优雅关闭，防止资源泄漏
 * 
 * @author VermouthX
 */
class StockerApp {

    /** 日志记录器 */
    private val log = Logger.getInstance(javaClass)

    /** 应用设置实例 */
    private val setting = StockerSetting.instance
    
    /** IntelliJ的消息总线，用于发布数据更新事件 */
    private val messageBus = ApplicationManager.getApplication().messageBus

    /** 调度线程池，用于执行定时刷新任务（单线程） */
    private var scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    
    /** 当前运行的定时任务引用，用于取消和重新调度 */
    private var scheduledFuture: ScheduledFuture<*>? = null

    /** 初始延迟时间（秒），首次启动时等待3秒，重启时立即执行 */
    private var scheduleInitialDelay: Long = 3

    /**
     * 启动定时刷新任务
     * 
     * 创建一个定时任务，按照设置的刷新间隔定期获取股票行情数据。
     * 支持重复调用，会先取消旧任务再启动新任务（用于动态调整刷新间隔）。
     * 
     * 执行流程：
     * 1. 取消现有的定时任务（如果有）
     * 2. 检查线程池状态，已关闭则重新创建
     * 3. 启动新的定时任务
     * 4. 保存任务引用以便后续取消
     * 
     * 任务内容：
     * - 获取所有收藏股票的实时行情（A股、港股、美股）
     * - 获取市场指数行情
     * - 通过消息总线发布更新事件
     * 
     * 性能优化：
     * - 只启动1个任务，合并所有市场数据请求
     * - 避免重复请求相同的数据
     */
    fun schedule() {
        // 先取消现有的任务（避免重复运行）
        scheduledFuture?.cancel(false)
        
        // 检查线程池是否已关闭，需要重新创建
        if (scheduledExecutorService.isShutdown) {
            scheduledExecutorService = Executors.newScheduledThreadPool(1)
            scheduleInitialDelay = 0  // 重启时立即执行，不延迟
        }
        
        // 启动定时刷新任务
        // - 初始延迟：scheduleInitialDelay秒
        // - 刷新间隔：从设置中动态获取（支持实时调整）
        // - 任务内容：获取所有市场的行情数据
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(
            createAllQuoteUpdateThread(), 
            scheduleInitialDelay, 
            setting.refreshInterval,  // 动态获取刷新间隔
            TimeUnit.SECONDS
        )
    }

    /**
     * 优雅关闭应用
     * 
     * 停止定时刷新任务并关闭线程池。
     * 使用优雅关闭策略，确保正在执行的任务有机会完成。
     * 
     * 关闭流程：
     * 1. 取消当前运行的定时任务
     * 2. 关闭线程池（不再接受新任务）
     * 3. 等待现有任务完成（最多10秒）
     * 4. 超时后强制关闭
     * 5. 处理中断异常
     * 
     * 优雅关闭的意义：
     * - 避免数据请求中途被打断
     * - 防止资源泄漏
     * - 确保线程正确释放
     */
    fun shutdown() {
        // 取消定时任务（允许当前执行完成）
        scheduledFuture?.cancel(true)
        
        // 关闭线程池
        scheduledExecutorService.shutdown()
        
        try {
            // 等待任务完成，最多等待10秒
            if (!scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                // 超时后强制关闭
                scheduledExecutorService.shutdownNow()
            }
        } catch (e: InterruptedException) {
            // 中断异常处理
            scheduledExecutorService.shutdownNow()
            Thread.currentThread().interrupt()  // 恢复中断状态
        }
    }

    /**
     * 检查应用是否已关闭
     * 
     * @return true表示线程池已关闭
     */
    fun isShutdown(): Boolean {
        return scheduledExecutorService.isShutdown
    }

    /**
     * 清空表格数据
     * 
     * 向所有市场的清空主题发布事件，通知监听器清空表格显示。
     * 用于刷新操作前清空旧数据。
     */
    private fun clear() {
        messageBus.syncPublisher(STOCK_ALL_QUOTE_RELOAD_TOPIC).clear()
        messageBus.syncPublisher(STOCK_CN_QUOTE_RELOAD_TOPIC).clear()
        messageBus.syncPublisher(STOCK_HK_QUOTE_RELOAD_TOPIC).clear()
        messageBus.syncPublisher(STOCK_US_QUOTE_RELOAD_TOPIC).clear()
    }

    /**
     * 关闭并清空
     * 
     * 用于用户手动刷新时：
     * 1. 先关闭现有任务
     * 2. 清空表格数据
     * 3. 准备重新加载
     * 
     * 典型使用场景：
     * - 用户点击刷新按钮
     * - 修改设置后重新加载
     */
    fun shutdownThenClear() {
        shutdown()
        clear()
    }

    /**
     * 创建统一的数据更新任务
     * 
     * 这是定时任务的核心执行逻辑。
     * 合并所有市场的数据请求，避免重复调用API。
     * 
     * 任务内容：
     * 1. 获取所有收藏股票的实时行情（A股、港股、美股）
     * 2. 获取所有市场指数的实时行情
     * 3. 合并数据并通过消息总线发布
     * 
     * 性能优化：
     * - 使用flatten()合并多个市场的数据
     * - 一次性发布所有数据，减少UI更新次数
     * - 避免重复请求相同的股票数据
     * 
     * @return Runnable任务对象，由定时任务执行器调度执行
     */
    private fun createAllQuoteUpdateThread(): Runnable {
        return Runnable {
            try {
                // 获取当前设置的数据提供商
                val quoteProvider = setting.quoteProvider
                log.info("Stocker: Starting quote update, provider: ${quoteProvider.title}")
                
                // 批量获取所有市场的股票行情
                // 使用flatten()将多个列表合并为一个列表
                val aShareQuotes = StockerQuoteHttpUtil.get(StockerMarketType.AShare, quoteProvider, setting.aShareList)
                val hkQuotes = StockerQuoteHttpUtil.get(StockerMarketType.HKStocks, quoteProvider, setting.hkStocksList)
                val usQuotes = StockerQuoteHttpUtil.get(StockerMarketType.USStocks, quoteProvider, setting.usStocksList)
                
                log.info("Stocker: Fetched quotes - A股: ${aShareQuotes.size}, 港股: ${hkQuotes.size}, 美股: ${usQuotes.size}")
                
                val allStockQuotes = listOf(
                    aShareQuotes,
                    hkQuotes,
                    usQuotes,
                    // 加密货币暂时注释，可按需启用
                    // StockerQuoteHttpUtil.get(StockerMarketType.Crypto, quoteProvider, setting.cryptoList)
                ).flatten()
                
                log.info("Stocker: Total stock quotes fetched: ${allStockQuotes.size}, expected: ${setting.allStockListSize}")
                
                // 批量获取所有市场的指数行情
                val aShareIndices = StockerQuoteHttpUtil.get(StockerMarketType.AShare, quoteProvider, StockerMarketIndex.CN.codes)
                val hkIndices = StockerQuoteHttpUtil.get(StockerMarketType.HKStocks, quoteProvider, StockerMarketIndex.HK.codes)
                val usIndices = StockerQuoteHttpUtil.get(StockerMarketType.USStocks, quoteProvider, StockerMarketIndex.US.codes)
                
                log.info("Stocker: Fetched indices - A股: ${aShareIndices.size}, 港股: ${hkIndices.size}, 美股: ${usIndices.size}")
                
                val allStockIndices = listOf(
                    aShareIndices,
                    hkIndices,
                    usIndices,
                    // StockerQuoteHttpUtil.get(StockerMarketType.Crypto, quoteProvider, StockerMarketIndex.Crypto.codes)
                ).flatten()
                
                log.info("Stocker: Total indices fetched: ${allStockIndices.size}")
                
                // 发布数据更新事件到消息总线
                val publisher = messageBus.syncPublisher(STOCK_ALL_QUOTE_UPDATE_TOPIC)
                log.info("Stocker: Publishing quotes to message bus, quotes count: ${allStockQuotes.size}")
                publisher.syncQuotes(allStockQuotes, setting.allStockListSize)  // 发布股票行情
                log.info("Stocker: Publishing indices to message bus, indices count: ${allStockIndices.size}")
                publisher.syncIndices(allStockIndices)  // 发布指数行情
                log.info("Stocker: Quote update completed successfully")
            } catch (e: Exception) {
                log.error("Stocker: Error in quote update thread", e)
            }
        }
    }

    /**
     * 创建单个市场的数据更新任务（已废弃）
     * 
     * 此方法保留用于兼容性，实际使用createAllQuoteUpdateThread()统一处理。
     * 
     * @param marketType 市场类型
     * @param stockCodeList 股票代码列表
     * @return Runnable任务对象
     */
    private fun createQuoteUpdateThread(marketType: StockerMarketType, stockCodeList: List<String>): Runnable {
        return Runnable {
            refresh(marketType, stockCodeList)
        }
    }

    /**
     * 刷新单个市场的行情数据（已废弃）
     * 
     * 此方法保留用于兼容性或特殊需求，正常情况下使用统一刷新。
     * 
     * 执行流程：
     * 1. 获取指定市场的股票行情
     * 2. 获取该市场的指数行情
     * 3. 发布到对应市场的更新主题
     * 
     * @param marketType 市场类型
     * @param stockCodeList 股票代码列表
     */
    private fun refresh(
        marketType: StockerMarketType, stockCodeList: List<String>
    ) {
        val quoteProvider = setting.quoteProvider
        val size = stockCodeList.size
        when (marketType) {
            StockerMarketType.AShare -> {
                val quotes = StockerQuoteHttpUtil.get(marketType, quoteProvider, stockCodeList)
                val indices = StockerQuoteHttpUtil.get(marketType, quoteProvider, StockerMarketIndex.CN.codes)
                val publisher = messageBus.syncPublisher(STOCK_CN_QUOTE_UPDATE_TOPIC)
                publisher.syncQuotes(quotes, size)
                publisher.syncIndices(indices)
            }

            StockerMarketType.HKStocks -> {
                val quotes = StockerQuoteHttpUtil.get(marketType, quoteProvider, stockCodeList)
                val indices = StockerQuoteHttpUtil.get(marketType, quoteProvider, StockerMarketIndex.HK.codes)
                val publisher = messageBus.syncPublisher(STOCK_HK_QUOTE_UPDATE_TOPIC)
                publisher.syncQuotes(quotes, size)
                publisher.syncIndices(indices)
            }

            StockerMarketType.USStocks -> {
                val quotes = StockerQuoteHttpUtil.get(marketType, quoteProvider, stockCodeList)
                val indices = StockerQuoteHttpUtil.get(marketType, quoteProvider, StockerMarketIndex.US.codes)
                val publisher = messageBus.syncPublisher(STOCK_US_QUOTE_UPDATE_TOPIC)
                publisher.syncQuotes(quotes, size)
                publisher.syncIndices(indices)
            }

            StockerMarketType.Crypto -> {
                val quotes = StockerQuoteHttpUtil.get(marketType, quoteProvider, stockCodeList)
                val indices = StockerQuoteHttpUtil.get(marketType, quoteProvider, StockerMarketIndex.Crypto.codes)
                val publisher = messageBus.syncPublisher(CRYPTO_QUOTE_UPDATE_TOPIC)
                publisher.syncQuotes(quotes, size)
                publisher.syncIndices(indices)
            }
        }
    }
}
