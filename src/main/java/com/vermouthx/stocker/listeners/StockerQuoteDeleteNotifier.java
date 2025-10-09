package com.vermouthx.stocker.listeners;

import com.intellij.util.messages.Topic;

/**
 * 股票删除通知接口
 * 
 * 定义股票删除事件的发布-订阅机制。
 * 当用户从收藏列表删除股票时，通过消息总线通知所有订阅者。
 * 
 * 发布-订阅模式：
 * - 发布者：StockerActionUtil.removeStock()（用户删除股票时）
 * - 订阅者：StockerQuoteDeleteListener（接收事件并更新表格）
 * - 消息总线：ApplicationManager.getApplication().messageBus
 * 
 * 主题分类：
 * - STOCK_ALL_QUOTE_DELETE_TOPIC：所有市场的删除事件（全局）
 * - STOCK_CN_QUOTE_DELETE_TOPIC：仅A股删除
 * - STOCK_HK_QUOTE_DELETE_TOPIC：仅港股删除
 * - STOCK_US_QUOTE_DELETE_TOPIC：仅美股删除
 * - CRYPTO_QUOTE_DELETE_TOPIC：仅加密货币删除
 * 
 * 使用场景：
 * - 股票搜索对话框删除股票
 * - 批量删除操作
 * - UI联动更新
 * 
 * @author VermouthX
 */
public interface StockerQuoteDeleteNotifier {
    
    /** 所有市场的股票删除主题（全局） */
    Topic<StockerQuoteDeleteNotifier> STOCK_ALL_QUOTE_DELETE_TOPIC = Topic.create("StockAllQuoteDeleteTopic", StockerQuoteDeleteNotifier.class);
    
    /** A股删除主题 */
    Topic<StockerQuoteDeleteNotifier> STOCK_CN_QUOTE_DELETE_TOPIC = Topic.create("StockCNQuoteDeleteTopic", StockerQuoteDeleteNotifier.class);
    
    /** 港股删除主题 */
    Topic<StockerQuoteDeleteNotifier> STOCK_HK_QUOTE_DELETE_TOPIC = Topic.create("StockHKQuoteDeleteTopic", StockerQuoteDeleteNotifier.class);
    
    /** 美股删除主题 */
    Topic<StockerQuoteDeleteNotifier> STOCK_US_QUOTE_DELETE_TOPIC = Topic.create("StockUSQuoteDeleteTopic", StockerQuoteDeleteNotifier.class);
    
    /** 加密货币删除主题 */
    Topic<StockerQuoteDeleteNotifier> CRYPTO_QUOTE_DELETE_TOPIC = Topic.create("CryptoQuoteDeleteTopic", StockerQuoteDeleteNotifier.class);

    /**
     * 删除后的回调方法
     * 
     * 发布者在完成删除操作后调用此方法。
     * 所有订阅者都会收到回调。
     * 
     * 订阅者需实现的操作：
     * - 从表格中移除对应行
     * - 清除相关的持久化数据
     * - 刷新汇总行
     * 
     * @param code 被删除的股票代码
     */
    void after(String code);
}
