package com.vermouthx.stocker.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.messages.MessageBus;
import com.vermouthx.stocker.entities.StockerSuggestion;
import com.vermouthx.stocker.enums.StockerMarketType;
import com.vermouthx.stocker.listeners.StockerQuoteDeleteNotifier;
import com.vermouthx.stocker.settings.StockerSetting;

/**
 * 股票操作工具类
 * 
 * 提供股票添加和删除的统一操作接口。
 * 主要用于股票搜索对话框中的添加/删除按钮行为。
 * 
 * 主要功能：
 * 1. 添加股票到收藏列表（带有效性验证）
 * 2. 从收藏列表删除股票
 * 3. 发布消息总线事件通知界面更新
 * 4. 持久化保存到设置中
 * 
 * 使用场景：
 * - 股票搜索对话框的Add/Delete按钮
 * - 批量添加股票操作
 * - 程序化管理收藏列表
 * 
 * @author VermouthX
 */
public class StockerActionUtil {
    
    /**
     * 添加股票到收藏列表
     * 
     * 执行流程：
     * 1. 检查股票是否已存在（避免重复添加）
     * 2. 验证股票代码有效性（调用API验证）
     * 3. 根据市场类型添加到对应列表
     * 4. 持久化保存到设置
     * 
     * 验证机制：
     * - 代码无效时显示错误对话框
     * - 代码已存在时直接返回false
     * 
     * @param market 股票所属市场类型
     * @param suggest 搜索建议对象（包含代码、名称、市场信息）
     * @param project 当前项目实例
     * @return true表示添加成功，false表示失败或已存在
     */
    public static boolean addStock(StockerMarketType market, StockerSuggestion suggest, Project project) {
        StockerSetting setting = StockerSetting.Companion.getInstance();
        String code = suggest.getCode();
        String fullName = suggest.getName();
        // 检查股票是否已存在
        if (!setting.containsCode(code)) {
            // 验证股票代码有效性（向API发送请求验证）
            if (StockerQuoteHttpUtil.INSTANCE.validateCode(market, setting.getQuoteProvider(), code)) {
                // 代码有效，根据市场类型添加到对应列表
                switch (market) {
                    case AShare:
                        return setting.getAShareList().add(code);
                    case HKStocks:
                        return setting.getHkStocksList().add(code);
                    case USStocks:
                        return setting.getUsStocksList().add(code);
                    case Crypto:
                        return setting.getCryptoList().add(code);
                }
            } else {
                // 代码无效，显示错误对话框提示用户
                String errMessage = fullName + " is not supported.";
                String errTitle = "Not Supported Stock";
                Messages.showErrorDialog(project, errMessage, errTitle);
                return false;
            }
        }
        // 股票已存在，返回false
        return false;
    }

    /**
     * 从收藏列表删除股票
     * 
     * 执行流程：
     * 1. 从设置中移除股票代码
     * 2. 发布删除消息到消息总线
     * 3. 通知相关监听器更新UI
     * 4. 清除该股票的成本价和持仓数量
     * 
     * 消息总线：
     * - 发布到对应市场的删除主题
     * - 同时发布到全局删除主题
     * - 确保所有订阅者都收到通知
     * 
     * @param market 股票所属市场类型
     * @param suggest 搜索建议对象（包含股票代码等信息）
     * @return true表示删除成功，false表示失败
     */
    public static boolean removeStock(StockerMarketType market, StockerSuggestion suggest) {
        StockerSetting setting = StockerSetting.Companion.getInstance();
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        
        // 从设置中移除股票代码
        setting.removeCode(market, suggest.getCode());
        
        // 获取对应市场的消息发布器
        StockerQuoteDeleteNotifier publisher = null;
        switch (market) {
            case AShare:
                // A股删除主题
                publisher = messageBus.syncPublisher(StockerQuoteDeleteNotifier.STOCK_CN_QUOTE_DELETE_TOPIC);
                break;
            case HKStocks:
                // 港股删除主题
                publisher = messageBus.syncPublisher(StockerQuoteDeleteNotifier.STOCK_HK_QUOTE_DELETE_TOPIC);
                break;
            case USStocks:
                // 美股删除主题
                publisher = messageBus.syncPublisher(StockerQuoteDeleteNotifier.STOCK_US_QUOTE_DELETE_TOPIC);
                break;
            case Crypto:
                // 加密货币删除主题
                publisher = messageBus.syncPublisher(StockerQuoteDeleteNotifier.CRYPTO_QUOTE_DELETE_TOPIC);
                break;
        }
        
        // 获取全局删除主题发布器
        StockerQuoteDeleteNotifier publisherToAll = messageBus.syncPublisher(StockerQuoteDeleteNotifier.STOCK_ALL_QUOTE_DELETE_TOPIC);
        
        if (publisher != null) {
            // 发布删除事件到所有订阅者
            // 1. 全局主题：通知所有窗口
            publisherToAll.after(suggest.getCode());
            // 2. 市场主题：通知对应市场的窗口
            publisher.after(suggest.getCode());
            return true;
        }
        return false;
    }
}
