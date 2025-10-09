package com.vermouthx.stocker.listeners;

import com.vermouthx.stocker.entities.StockerQuote;
import com.vermouthx.stocker.utils.StockerTableModelUtil;
import com.vermouthx.stocker.views.StockerTableView;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * 股票行情更新监听器
 * 
 * 订阅股票行情更新事件，当定时任务获取到新的行情数据时，
 * 负责更新表格显示的股票价格、涨跌幅、盈亏等信息。
 * 
 * 工作原理：
 * 1. 监听消息总线的行情更新主题
 * 2. 接收到新行情数据
 * 3. 更新表格中的对应行
 * 4. 计算并更新盈亏数据
 * 5. 刷新汇总行
 * 
 * 更新策略：
 * - 股票已存在：更新现有行的数据
 * - 股票不存在：添加新行（仅在初始加载时）
 * - 增量更新：只更新变化的单元格，提升性能
 * 
 * 线程安全：
 * - 使用synchronized保护表格模型操作
 * - 确保UI更新在EDT线程执行
 * 
 * @author VermouthX
 */
public class StockerQuoteUpdateListener implements StockerQuoteUpdateNotifier {
    /** 关联的表格视图 */
    private final StockerTableView myTableView;

    /**
     * 构造函数
     * 
     * @param myTableView 要更新的表格视图实例
     */
    public StockerQuoteUpdateListener(StockerTableView myTableView) {
        this.myTableView = myTableView;
    }

    /**
     * 同步股票行情数据
     * 
     * 当定时任务获取到新的行情数据时被调用。
     * 遍历所有行情，更新表格中对应的行。
     * 
     * 处理逻辑：
     * 1. 查找股票在表格中的位置
     * 2. 如果存在：增量更新变化的字段
     * 3. 如果不存在：添加新行（初始加载时）
     * 4. 更新盈亏计算
     * 5. 刷新汇总行
     * 
     * 性能优化：
     * - 只更新变化的单元格
     * - 使用fireTableCellUpdated而非fireTableDataChanged
     * - 避免不必要的UI重绘
     * 
     * @param quotes 股票行情列表
     * @param size 预期的股票数量
     */
    @Override
    public void syncQuotes(List<StockerQuote> quotes, int size) {
        DefaultTableModel tableModel = myTableView.getTableModel();
        
        // 遍历所有收到的股票行情
        quotes.forEach(quote -> {
            // 使用synchronized保证线程安全，避免并发修改表格
            synchronized (myTableView.getTableModel()) {
                // 查找该股票在表格中的位置
                int rowIndex = StockerTableModelUtil.existAt(tableModel, quote.getCode());
                
                if (rowIndex != -1) {
                    // === 股票已存在：增量更新变化的字段 ===
                    
                    // 更新股票名称（索引1）
                    if (!tableModel.getValueAt(rowIndex, 1).equals(quote.getName())) {
                        tableModel.setValueAt(quote.getName(), rowIndex, 1);
                        tableModel.fireTableCellUpdated(rowIndex, 1);
                    }
                    
                    // 更新当前价格（索引2）
                    if (!tableModel.getValueAt(rowIndex, 2).equals(quote.getCurrent())) {
                        tableModel.setValueAt(quote.getCurrent(), rowIndex, 2);
                        tableModel.fireTableCellUpdated(rowIndex, 2);
                    }
                    
                    // 更新涨跌幅（索引3）
                    if (!tableModel.getValueAt(rowIndex, 3).equals(quote.getPercentage())) {
                        tableModel.setValueAt(quote.getPercentage() + "%", rowIndex, 3);
                        tableModel.fireTableCellUpdated(rowIndex, 3);
                    }
                    // === 确保显示持久化的成本价 ===
                    int costCol = myTableView.getTableBody().getColumn("Cost Price").getModelIndex();
                    Object currentCost = tableModel.getValueAt(rowIndex, costCol);
                    // 从设置中读取用户保存的成本价
                    String persisted = com.vermouthx.stocker.settings.StockerSetting.Companion.getInstance().getCost(quote.getCode());
                    String costToShow = persisted == null ? "" : persisted;
                    
                    // 如果成本价有变化，更新显示
                    if (currentCost == null || !currentCost.toString().equals(costToShow)) {
                        tableModel.setValueAt(costToShow, rowIndex, costCol);
                        tableModel.fireTableCellUpdated(rowIndex, costCol);
                    }
                    
                    // === 确保显示持久化的持仓数量 ===
                    int qtyCol = myTableView.getTableBody().getColumn("Quantity").getModelIndex();
                    Object currentQty = tableModel.getValueAt(rowIndex, qtyCol);
                    // 从设置中读取用户保存的持仓数量
                    String persistedQty = com.vermouthx.stocker.settings.StockerSetting.Companion.getInstance().getQuantity(quote.getCode());
                    String qtyToShow = persistedQty == null ? "" : persistedQty;
                    
                    // 如果持仓数量有变化，更新显示
                    if (currentQty == null || !currentQty.toString().equals(qtyToShow)) {
                        tableModel.setValueAt(qtyToShow, rowIndex, qtyCol);
                        tableModel.fireTableCellUpdated(rowIndex, qtyCol);
                    }
                    
                    // === 计算并更新盈亏数据 ===
                    try {
                        int dayPlCol = myTableView.getTableBody().getColumn("Day P&L").getModelIndex();
                        int plCol = myTableView.getTableBody().getColumn("P&L").getModelIndex();
                        
                        double current = quote.getCurrent();  // 当前价格
                        double c = costToShow.isBlank() ? 0.0 : Double.parseDouble(costToShow);  // 成本价
                        double q = qtyToShow.isBlank() ? 0.0 : Double.parseDouble(qtyToShow);    // 持仓数量
                        
                        // 计算总盈亏 = (当前价 - 成本价) × 持仓数量
                        double pl = (current - c) * q;
                        tableModel.setValueAt(String.format("%.2f", pl), rowIndex, plCol);
                        tableModel.fireTableCellUpdated(rowIndex, plCol);
                        
                        // 计算日盈亏 = 涨跌额 × 持仓数量
                        double change = quote.getChange();
                        double dayPl = change * q;
                        tableModel.setValueAt(String.format("%.2f", dayPl), rowIndex, dayPlCol);
                        tableModel.fireTableCellUpdated(rowIndex, dayPlCol);
                    } catch (Exception ignore) {
                        // 解析失败时忽略（保持原值）
                    }
                    
                    // 更新完成后刷新汇总行
                    myTableView.updateSummaryRowPublic();
                } else {
                    // === 股票不存在：添加新行 ===
                    // 只在初始加载时添加（quotes数量等于预期数量时）
                    if (quotes.size() == size) {
                        // 读取持久化的成本价和持仓数量
                        String cost = com.vermouthx.stocker.settings.StockerSetting.Companion.getInstance().getCost(quote.getCode());
                        String qty = com.vermouthx.stocker.settings.StockerSetting.Companion.getInstance().getQuantity(quote.getCode());
                        
                        // 解析数值用于计算盈亏
                        double c = (cost == null || cost.isBlank()) ? 0.0 : Double.parseDouble(cost);
                        double q = (qty == null || qty.isBlank()) ? 0.0 : Double.parseDouble(qty);
                        
                        // 计算盈亏
                        double pl = (quote.getCurrent() - c) * q;      // 总盈亏
                        double dayPl = quote.getChange() * q;          // 日盈亏
                        
                        // 添加新行到表格
                        // 列顺序：代码, 名称, 现价, 涨跌幅, 成本价, 持仓数量, 日盈亏, 总盈亏
                        tableModel.addRow(new Object[]{
                            quote.getCode(), 
                            quote.getName(), 
                            quote.getCurrent(), 
                            quote.getPercentage() + "%", 
                            cost == null ? "" : cost, 
                            qty == null ? "" : qty, 
                            String.format("%.2f", dayPl), 
                            String.format("%.2f", pl)
                        });
                        
                        // 添加新行后刷新汇总行
                        myTableView.updateSummaryRowPublic();
                    }
                }
            }
        });
    }

    /**
     * 同步市场指数数据
     * 
     * 当定时任务获取到新的指数数据时被调用。
     * 更新工具窗口底部的指数显示面板。
     * 
     * @param indices 市场指数行情列表
     */
    @Override
    public void syncIndices(List<StockerQuote> indices) {
        // 使用synchronized保证线程安全
        synchronized (myTableView) {
            myTableView.syncIndices(indices);
        }
    }

}
