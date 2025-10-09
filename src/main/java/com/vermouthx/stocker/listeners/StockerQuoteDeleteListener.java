package com.vermouthx.stocker.listeners;

import com.vermouthx.stocker.utils.StockerTableModelUtil;
import com.vermouthx.stocker.views.StockerTableView;

import javax.swing.table.DefaultTableModel;

/**
 * 股票删除监听器
 * 
 * 订阅股票删除事件，当用户删除收藏的股票时，
 * 负责从表格中移除对应的行，并清理相关的持久化数据。
 * 
 * 工作原理：
 * 1. 监听消息总线的删除事件
 * 2. 从表格中移除对应行
 * 3. 清除持久化的成本价和持仓数量
 * 4. 刷新汇总行
 * 
 * 删除流程：
 * - 用户在搜索对话框点击Delete按钮
 * - StockerActionUtil.removeStock()发布删除事件
 * - 本监听器收到事件并执行删除操作
 * - 表格UI即时更新
 * 
 * 线程安全：
 * - synchronized保护表格模型操作
 * - 使用SwingUtilities.invokeLater确保UI操作在EDT线程
 * 
 * @author VermouthX
 */
public class StockerQuoteDeleteListener implements StockerQuoteDeleteNotifier {

    /** 关联的表格视图 */
    private final StockerTableView myTableView;

    /**
     * 构造函数
     * 
     * @param myTableView 要操作的表格视图实例
     */
    public StockerQuoteDeleteListener(StockerTableView myTableView) {
        this.myTableView = myTableView;
    }

    /**
     * 删除后的回调处理
     * 
     * 当股票从收藏列表中删除后被调用。
     * 执行表格行删除和数据清理操作。
     * 
     * 执行步骤：
     * 1. 查找股票在表格中的位置
     * 2. 从表格中删除该行
     * 3. 清除持久化的成本价
     * 4. 清除持久化的持仓数量
     * 5. 刷新汇总行
     * 
     * 数据清理：
     * - 成本价设置为null
     * - 持仓数量设置为null
     * - 确保下次添加时从干净状态开始
     * 
     * @param code 被删除的股票代码
     */
    @Override
    public void after(String code) {
        // 使用synchronized保证线程安全
        synchronized (myTableView.getTableModel()) {
            DefaultTableModel tableModel = myTableView.getTableModel();
            
            // 查找股票在表格中的行索引
            int rowIndex = StockerTableModelUtil.existAt(tableModel, code);
            
            if (rowIndex != -1) {
                // 找到了，从表格中删除该行
                tableModel.removeRow(rowIndex);
                // 触发表格行删除事件，通知UI更新
                tableModel.fireTableRowsDeleted(rowIndex, rowIndex);
            }
        }
        
        // 清除持久化的成本价数据
        com.vermouthx.stocker.settings.StockerSetting.Companion.getInstance().setCost(code, null);
        
        // 清除持久化的持仓数量数据
        com.vermouthx.stocker.settings.StockerSetting.Companion.getInstance().setQuantity(code, null);
        
        // 异步刷新汇总行（确保在EDT线程执行）
        javax.swing.SwingUtilities.invokeLater(() -> myTableView.updateSummaryRowPublic());
    }

}
