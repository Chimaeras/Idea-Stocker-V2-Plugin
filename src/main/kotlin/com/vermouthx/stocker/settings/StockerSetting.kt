package com.vermouthx.stocker.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteColorPattern
import com.vermouthx.stocker.enums.StockerQuoteProvider

/**
 * 插件设置管理类（单例）
 * 
 * 负责管理插件的所有配置和用户数据。
 * 使用IntelliJ的持久化组件机制，数据自动保存到XML文件。
 * 
 * 核心功能：
 * 1. 管理用户收藏的股票列表（A股、港股、美股等）
 * 2. 管理每个股票的成本价和持仓数量
 * 3. 管理插件配置（数据源、刷新间隔、颜色方案等）
 * 4. 提供线程安全的数据访问
 * 
 * 持久化机制：
 * - 配置文件：stocker-config.xml
 * - 存储位置：IntelliJ配置目录
 * - 自动保存：修改后自动写入文件
 * - 自动加载：启动时自动读取
 * 
 * 数据结构：
 * - 插件版本
 * - 数据提供商选择
 * - 颜色方案选择
 * - 刷新间隔设置
 * - 各市场的股票代码列表
 * - 成本价映射（股票代码 -> 成本价）
 * - 持仓数量映射（股票代码 -> 数量）
 * 
 * @author VermouthX
 */
@State(name = "Stocker", storages = [Storage("stocker-config.xml")])
class StockerSetting : PersistentStateComponent<StockerSettingState> {
    
    /** 内部状态对象，存储实际的配置数据 */
    private var myState = StockerSettingState()

    /** 日志记录器 */
    private val log = Logger.getInstance(javaClass)

    companion object {
        /**
         * 获取插件设置的单例实例
         * 
         * 使用IntelliJ的服务机制获取单例。
         * 确保全局只有一个设置实例。
         * 
         * @return 插件设置单例
         */
        val instance: StockerSetting
            get() = ApplicationManager.getApplication().getService(StockerSetting::class.java)
    }

    var version: String
        get() = myState.version
        set(value) {
            myState.version = value
            log.info("Stocker updated to $value")
        }

    var quoteProvider: StockerQuoteProvider
        get() = myState.quoteProvider
        set(value) {
            myState.quoteProvider = value
            log.info("Stocker quote provider switched to ${value.title}")
        }

    var quoteColorPattern: StockerQuoteColorPattern
        get() = myState.quoteColorPattern
        set(value) {
            myState.quoteColorPattern = value
            log.info("Stocker quote color pattern switched to ${value.title}")
        }

    var refreshInterval: Long
        get() = myState.refreshInterval
        set(value) {
            myState.refreshInterval = value
            log.info("Stocker refresh interval set to $value")
        }

    var aShareList: MutableList<String>
        get() = myState.aShareList
        set(value) {
            myState.aShareList = value
        }

    var hkStocksList: MutableList<String>
        get() = myState.hkStocksList
        set(value) {
            myState.hkStocksList = value
        }

    var usStocksList: MutableList<String>
        get() = myState.usStocksList
        set(value) {
            myState.usStocksList = value
        }

    var cryptoList: MutableList<String>
        get() = myState.cryptoList
        set(value) {
            myState.cryptoList = value
        }

    val allStockListSize: Int
        get() = aShareList.size + hkStocksList.size + usStocksList.size + cryptoList.size

    var costPriceMap: MutableMap<String, String>
        get() = myState.costPriceMap
        set(value) {
            myState.costPriceMap = value
        }

    fun getCost(code: String): String? = myState.costPriceMap[code]

    fun setCost(code: String, cost: String?) {
        if (cost == null || cost.isBlank()) {
            myState.costPriceMap.remove(code)
        } else {
            myState.costPriceMap[code] = cost
        }
    }

    var quantityMap: MutableMap<String, String>
        get() = myState.quantityMap
        set(value) {
            myState.quantityMap = value
        }

    fun getQuantity(code: String): String? = myState.quantityMap[code]

    fun setQuantity(code: String, qty: String?) {
        if (qty == null || qty.isBlank()) {
            myState.quantityMap.remove(code)
        } else {
            myState.quantityMap[code] = qty
        }
    }

    fun containsCode(code: String): Boolean {
        return aShareList.contains(code) ||
                hkStocksList.contains(code) ||
                usStocksList.contains(code) ||
                cryptoList.contains(code)
    }

    fun marketOf(code: String): StockerMarketType? {
        if (aShareList.contains(code)) {
            return StockerMarketType.AShare
        }
        if (hkStocksList.contains(code)) {
            return StockerMarketType.HKStocks
        }
        if (usStocksList.contains(code)) {
            return StockerMarketType.USStocks
        }
        if (cryptoList.contains(code)) {
            return StockerMarketType.Crypto
        }
        return null
    }

    fun removeCode(market: StockerMarketType, code: String) {
        when (market) {
            StockerMarketType.AShare -> {
                synchronized(aShareList) {
                    aShareList.remove(code)
                }
            }

            StockerMarketType.HKStocks -> {
                synchronized(hkStocksList) {
                    hkStocksList.remove(code)
                }
            }

            StockerMarketType.USStocks -> {
                synchronized(usStocksList) {
                    usStocksList.remove(code)
                }
            }

            StockerMarketType.Crypto -> {
                synchronized(cryptoList) {
                    cryptoList.remove(code)
                }
            }
        }
    }

    override fun getState(): StockerSettingState {
        return myState
    }

    override fun loadState(state: StockerSettingState) {
        myState = state
    }

}
