package com.vermouthx.stocker.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * 默认表格单元格渲染器
 * 
 * 扩展DefaultTableCellRenderer，提供统一的单元格样式。
 * 作为其他特定列渲染器的基类。
 * 
 * 样式定制：
 * 1. 文字居中对齐
 * 2. 继承默认的选中和焦点样式
 * 
 * 继承链：
 * DefaultTableCellRenderer <- StockerDefaultTableCellRender <- 具体列渲染器
 * 
 * 使用场景：
 * - 作为基类被其他渲染器继承
 * - 提供统一的样式基础
 * - 简化代码复用
 * 
 * @author VermouthX
 */
public class StockerDefaultTableCellRender extends DefaultTableCellRenderer {

    /**
     * 渲染表格单元格
     * 
     * 设置单元格的文字居中对齐。
     * 子类可以重写此方法添加更多自定义样式：
     * - 颜色（涨跌颜色）
     * - 字体（加粗）
     * - 特殊格式化
     * 
     * @param table 表格实例
     * @param value 单元格值
     * @param isSelected 是否选中
     * @param hasFocus 是否有焦点
     * @param row 行索引
     * @param column 列索引
     * @return 渲染后的组件
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // 设置文字居中对齐
        setHorizontalAlignment(SwingConstants.CENTER);
        
        // 调用父类方法完成基础渲染
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
