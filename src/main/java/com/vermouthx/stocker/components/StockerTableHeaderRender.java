package com.vermouthx.stocker.components;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * 表格表头渲染器
 * 
 * 自定义表格表头的显示样式，主要功能包括：
 * 1. 表头文字居中对齐
 * 2. 显示排序指示器（升序▲/降序▼）
 * 3. 动态更新排序箭头
 * 
 * 视觉效果：
 * - 未排序的列：只显示列名
 * - 升序排序：列名 ▲
 * - 降序排序：列名 ▼
 * 
 * 使用场景：
 * - 股票表格的表头显示
 * - 提供可视化的排序状态反馈
 * 
 * @author VermouthX
 */
public class StockerTableHeaderRender implements TableCellRenderer {

    /** 默认的表头渲染器，用于继承原有样式 */
    private final TableCellRenderer renderer;

    /**
     * 构造函数
     * 
     * 获取并保存表格默认的表头渲染器。
     * 在此基础上添加自定义样式，保持一致性。
     * 
     * @param table 表格实例
     */
    public StockerTableHeaderRender(JTable table) {
        renderer = table.getTableHeader().getDefaultRenderer();
    }

    /**
     * 渲染表头单元格
     * 
     * 执行步骤：
     * 1. 调用默认渲染器获取基础组件
     * 2. 设置文字居中对齐
     * 3. 检查是否有排序状态
     * 4. 为排序列添加箭头指示器
     * 
     * @param table 表格实例
     * @param value 单元格值（列名）
     * @param isSelected 是否选中
     * @param hasFocus 是否有焦点
     * @param row 行索引（表头固定为0）
     * @param column 列索引
     * @return 渲染后的组件
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // 使用默认渲染器获取基础组件
        JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        // 设置文字居中对齐（所有列头都居中）
        label.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 添加排序指示器
        if (table.getRowSorter() != null) {
            List<? extends RowSorter.SortKey> sortKeys = table.getRowSorter().getSortKeys();
            if (!sortKeys.isEmpty()) {
                // 获取当前排序的列索引（模型索引转视图索引）
                int sortedColumn = table.convertColumnIndexToView(sortKeys.get(0).getColumn());
                
                // 如果当前列正在排序，添加箭头指示器
                if (column == sortedColumn) {
                    // 根据排序方向选择箭头：升序▲ 降序▼
                    String arrow = sortKeys.get(0).getSortOrder() == SortOrder.ASCENDING ? " ▲" : " ▼";
                    label.setText(value + arrow);
                }
            }
        }
        
        return label;
    }
}
