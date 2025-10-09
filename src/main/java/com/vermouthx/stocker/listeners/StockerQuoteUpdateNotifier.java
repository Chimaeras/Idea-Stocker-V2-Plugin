package com.vermouthx.stocker.listeners;

import com.intellij.util.messages.Topic;
import com.vermouthx.stocker.entities.StockerQuote;

import java.util.List;

/**
 * 股票行情更新通知接口
 * 
 * 定义行情数据更新的发布-订阅机制。
 * 使用IntelliJ的消息总线（Message Bus）实现松耦合的事件通信。
 * 
 * 发布-订阅模式：
 * - 发布者：StockerApp（定时任务获取到新数据后发布）
 * - 订阅者：StockerQuoteUpdateListener（接收事件并更新UI）
 * - 消息总线：ApplicationManager.getApplication().messageBus
 * 
 * 主题分类：
 * - STOCK_ALL_QUOTE_UPDATE_TOPIC：所有股票的更新（推荐使用）
 * - STOCK_CN_QUOTE_UPDATE_TOPIC：仅A股更新
 * - STOCK_HK_QUOTE_UPDATE_TOPIC：仅港股更新
 * - STOCK_US_QUOTE_UPDATE_TOPIC：仅美股更新
 * - CRYPTO_QUOTE_UPDATE_TOPIC：仅加密货币更新
 * 
 * 优势：
 * - 解耦数据获取和UI更新
 * - 支持多个订阅者同时监听
 * - 便于扩展和测试
 * 
 * @author VermouthX
 */
public interface StockerQuoteUpdateNotifier {
    
    /** 所有市场的股票行情更新主题（全局） */
    Topic<StockerQuoteUpdateNotifier> STOCK_ALL_QUOTE_UPDATE_TOPIC = Topic.create("StockAllQuoteUpdateTopic", StockerQuoteUpdateNotifier.class);
    
    /** A股行情更新主题 */
    Topic<StockerQuoteUpdateNotifier> STOCK_CN_QUOTE_UPDATE_TOPIC = Topic.create("StockCNQuoteUpdateTopic", StockerQuoteUpdateNotifier.class);
    
    /** 港股行情更新主题 */
    Topic<StockerQuoteUpdateNotifier> STOCK_HK_QUOTE_UPDATE_TOPIC = Topic.create("StockHKQuoteUpdateTopic", StockerQuoteUpdateNotifier.class);
    
    /** 美股行情更新主题 */
    Topic<StockerQuoteUpdateNotifier> STOCK_US_QUOTE_UPDATE_TOPIC = Topic.create("StockUSQuoteUpdateTopic", StockerQuoteUpdateNotifier.class);
    
    /** 加密货币行情更新主题 */
    Topic<StockerQuoteUpdateNotifier> CRYPTO_QUOTE_UPDATE_TOPIC = Topic.create("CryptoQuoteUpdateTopic", StockerQuoteUpdateNotifier.class);

    /**
     * 同步股票行情数据
     * 
     * 发布者调用此方法发送新的行情数据。
     * 所有订阅者都会收到此方法的调用。
     * 
     * @param quotes 股票行情列表
     * @param size 预期的股票数量（用于判断是否为完整加载）
     */
    void syncQuotes(List<StockerQuote> quotes, int size);

    /**
     * 同步市场指数数据
     * 
     * 发布者调用此方法发送新的指数数据。
     * 更新工具窗口底部的指数显示面板。
     * 
     * @param indices 市场指数行情列表
     */
    void syncIndices(List<StockerQuote> indices);
}
