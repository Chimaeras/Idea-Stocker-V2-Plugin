package com.vermouthx.stocker.components;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.Comparator;

/**
 * 自定义表格排序器
 * 
 * 扩展TableRowSorter，提供智能排序功能。
 * 
 * 核心特性：
 * 1. 智能识别数字和文本列，自动选择合适的排序方式
 * 2. 汇总行始终固定在第一行，不参与排序
 * 3. 数字列中0值始终排在最后（无论升序降序）
 * 4. 自动处理百分号等特殊符号
 * 
 * 排序规则：
 * - 汇总行：永远在第一行（通过比较器识别）
 * - 数字列：0值在最后，非0值按数值大小排序
 * - 文本列：按字符串自然顺序排序
 * - 空值：排在非空值前面
 * 
 * 使用场景：
 * - 股票表格的列排序
 * - 点击表头切换升序/降序
 * - 保持汇总行位置固定
 * 
 * @author VermouthX
 */
public class StockerTableSorter extends TableRowSorter<TableModel> {

    /**
     * 构造函数
     * 
     * 初始化排序器并为每一列设置智能比较器。
     * 
     * @param model 表格数据模型
     */
    public StockerTableSorter(TableModel model) {
        super(model);
        initComparators();  // 初始化比较器
    }

    /**
     * 初始化所有列的比较器
     * 
     * 为表格的每一列设置SmartComparator实例。
     * 每个比较器都绑定到特定的列索引，用于访问排序状态。
     */
    private void initComparators() {
        // 遍历所有列
        for (int i = 0; i < getModel().getColumnCount(); i++) {
            final int columnIndex = i;
            // 为每列设置智能比较器
            setComparator(i, new SmartComparator(this, columnIndex));
        }
    }

    /**
     * 智能比较器内部类
     * 
     * 实现自适应的排序逻辑，能够：
     * 1. 自动识别数字和文本数据
     * 2. 特殊处理汇总行（固定在第一行）
     * 3. 特殊处理0值（固定在最后）
     * 4. 根据排序方向调整比较结果
     * 
     * 比较优先级（从高到低）：
     * 1. 汇总行检查（最高优先级）
     * 2. null值处理
     * 3. 空字符串处理
     * 4. 数字解析和0值处理
     * 5. 普通数值比较或字符串比较
     * 
     * @author VermouthX
     */
    private static class SmartComparator implements Comparator<Object> {
        /** 排序器引用，用于获取排序状态 */
        private final StockerTableSorter sorter;
        
        /** 当前比较器对应的列索引 */
        private final int columnIndex;

        /**
         * 构造函数
         * 
         * @param sorter 排序器实例
         * @param columnIndex 列索引
         */
        public SmartComparator(StockerTableSorter sorter, int columnIndex) {
            this.sorter = sorter;
            this.columnIndex = columnIndex;
        }

        /**
         * 比较两个对象
         * 
         * 实现智能比较逻辑，处理多种特殊情况：
         * 
         * 返回值含义：
         * - 负数：o1 < o2（o1排在o2前面）
         * - 0：o1 = o2（相等）
         * - 正数：o1 > o2（o1排在o2后面）
         * 
         * 注意：降序排序时，排序器会自动反转比较结果
         * 
         * @param o1 第一个对象
         * @param o2 第二个对象
         * @return 比较结果
         */
        @Override
        public int compare(Object o1, Object o2) {
            // === 第1优先级：null值处理 ===
            if (o1 == null && o2 == null) return 0;  // 都是null，相等
            if (o1 == null) return -1;               // o1是null，排在前面
            if (o2 == null) return 1;                // o2是null，排在前面

            String s1 = o1.toString().trim();
            String s2 = o2.toString().trim();
            
            // === 第2优先级：汇总行处理（最高优先级，永远在第一行） ===
            // 不仅检查当前值，还要检查该值是否来自汇总行
            // 这样即使数字列（如"0.00"）也能正确识别汇总行
            boolean isSummary1 = isFromSummaryRow(o1);
            boolean isSummary2 = isFromSummaryRow(o2);
            
            if (isSummary1 && isSummary2) return 0;  // 都是汇总行，相等
            
            // 检查当前排序方向来决定汇总行的比较结果
            boolean isDescForSummary = isCurrentSortDescending();
            
            if (isDescForSummary) {
                // 降序时：汇总行要排最前，需要让汇总行"最大"
                // 降序排序：大的在前 -> 汇总行要表现为最大
                if (isSummary1) return 1;   // o1是汇总行，o1>o2，降序时排前面
                if (isSummary2) return -1;  // o2是汇总行，o1<o2，降序时o2排前面
            } else {
                // 升序时：汇总行要排最前，需要让汇总行"最小"
                // 升序排序：小的在前 -> 汇总行要表现为最小
                if (isSummary1) return -1;  // o1是汇总行，o1<o2，升序时排前面
                if (isSummary2) return 1;   // o2是汇总行，o1>o2，升序时o2排前面
            }

            // === 第3优先级：空字符串处理 ===
            if (s1.isEmpty() && s2.isEmpty()) return 0;  // 都是空字符串，相等
            if (s1.isEmpty()) return -1;                 // o1是空，排在前面
            if (s2.isEmpty()) return 1;                  // o2是空，排在前面

            // === 第4优先级：数字解析和比较 ===
            
            // 清理特殊符号（如百分号），便于解析数字
            // 示例："5.25%" -> "5.25"
            String cleanS1 = s1.replace("%", "").trim();
            String cleanS2 = s2.replace("%", "").trim();

            // 尝试将字符串解析为数字
            try {
                Double d1 = Double.parseDouble(cleanS1);
                Double d2 = Double.parseDouble(cleanS2);
                
                // 检查当前排序方向（用于0值处理）
                boolean isDescending = isCurrentSortDescending();
                
                // === 特殊处理：0值排在最后（无论升序还是降序） ===
                // 使用小误差判断0值，考虑浮点数精度问题
                boolean isZero1 = Math.abs(d1) < 0.0001;
                boolean isZero2 = Math.abs(d2) < 0.0001;
                
                if (isZero1 && isZero2) return 0;  // 都是0，相等
                
                if (isDescending) {
                    // 降序排序：大->小，0值要在最后
                    // 让0表现为"最小值"，降序时最小的排最后
                    if (isZero1) return -1;  // d1是0，o1<o2，降序时排后面
                    if (isZero2) return 1;   // d2是0，o1>o2，降序时o2排后面
                } else {
                    // 升序排序：小->大，0值要在最后
                    // 让0表现为"最大值"，升序时最大的排最后
                    if (isZero1) return 1;   // d1是0，o1>o2，升序时排后面
                    if (isZero2) return -1;  // d2是0，o1<o2，升序时o2排后面
                }
                
                // === 第5优先级：普通数值比较 ===
                // 都不是0，按正常数值大小比较
                return d1.compareTo(d2);
                
            } catch (NumberFormatException e) {
                // === 第6优先级：文本比较 ===
                // 如果不是数字（解析失败），按字符串自然顺序比较
                return s1.compareTo(s2);
            }
        }

        /**
         * 检查某个值是否来自汇总行
         * 
         * 判断逻辑：
         * 1. 快速检查：如果值是"Total"，直接判定为汇总行
         * 2. 模型遍历：查找第一列为"SUMMARY"的行
         * 3. 值比对：检查该行当前列的值是否与参数值匹配
         * 
         * 为什么需要遍历模型：
         * - 数字列的值（如"0.00"）可能与普通行相同
         * - 需要通过行标识（第一列="SUMMARY"）来准确判断
         * 
         * 性能考虑：
         * - 优先快速检查（避免不必要的遍历）
         * - 遍历在排序时执行，频率可接受
         * - 使用try-catch保证稳定性
         * 
         * @param value 要检查的值
         * @return true表示该值来自汇总行
         */
        private boolean isFromSummaryRow(Object value) {
            if (value == null) return false;
            
            // 快速检查：如果值本身是汇总行的特征文本（名称列）
            String valueStr = value.toString();
            if ("Total".equals(valueStr)) {
                return true;
            }
            
            try {
                TableModel model = sorter.getModel();
                // 遍历所有行，查找汇总行
                for (int row = 0; row < model.getRowCount(); row++) {
                    // 检查第一列是否为"SUMMARY"（汇总行标识）
                    Object firstCol = model.getValueAt(row, 0);
                    if (firstCol != null && "SUMMARY".equals(firstCol.toString())) {
                        // 找到汇总行，检查当前列的值是否与参数值匹配
                        Object cellValue = model.getValueAt(row, columnIndex);
                        
                        // 多种匹配方式：
                        // 1. 对象引用相等
                        // 2. equals方法相等
                        // 3. 字符串表示相等（处理数字对象）
                        if (cellValue == value || 
                            (cellValue != null && cellValue.equals(value)) ||
                            (cellValue != null && cellValue.toString().equals(value.toString()))) {
                            return true;
                        }
                    }
                }
            } catch (Exception ignore) {
                // 异常时返回false，避免影响排序
            }
            return false;
        }

        /**
         * 检查当前列的排序是否为降序
         * 
         * 通过查询排序器的SortKey列表获取当前排序状态。
         * 用于0值和汇总行的特殊处理逻辑。
         * 
         * @return true表示当前列正在降序排序
         */
        private boolean isCurrentSortDescending() {
            try {
                java.util.List<? extends RowSorter.SortKey> sortKeys = sorter.getSortKeys();
                if (!sortKeys.isEmpty()) {
                    RowSorter.SortKey sortKey = sortKeys.get(0);
                    // 检查是否是当前列的排序
                    if (sortKey.getColumn() == columnIndex) {
                        return sortKey.getSortOrder() == SortOrder.DESCENDING;
                    }
                }
            } catch (Exception ignore) {
                // 异常时返回false（假设为升序）
            }
            return false;
        }
    }

    /**
     * 判断列是否可排序
     * 
     * 重写此方法以控制哪些列允许排序。
     * 目前所有列都允许排序。
     * 
     * @param column 列索引
     * @return true表示该列可排序
     */
    @Override
    public boolean isSortable(int column) {
        // 所有列都可以排序
        return true;
    }
}

