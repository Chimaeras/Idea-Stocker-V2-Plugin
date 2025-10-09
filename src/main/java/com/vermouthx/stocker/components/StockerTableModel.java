package com.vermouthx.stocker.components;

import javax.swing.table.DefaultTableModel;

/**
 * 自定义表格数据模型
 * 
 * 继承自DefaultTableModel，定制表格的编辑行为。
 * 只允许用户编辑成本价和持仓数量两列，其他列都是只读的。
 * 
 * 列索引定义：
 * - 0: Symbol（股票代码）- 隐藏列，不可编辑
 * - 1: Name（股票名称）- 不可编辑
 * - 2: Current（当前价格）- 不可编辑（实时数据）
 * - 3: Change%（涨跌幅）- 不可编辑（实时数据）
 * - 4: Cost Price（成本价）- ✓可编辑（用户输入）
 * - 5: Quantity（持仓数量）- ✓可编辑（用户输入）
 * - 6: Day P&L（日盈亏）- 不可编辑（自动计算）
 * - 7: P&L（总盈亏）- 不可编辑（自动计算）
 * 
 * 编辑逻辑：
 * - 用户可以双击成本价或持仓数量单元格进行编辑
 * - 编辑后自动计算盈亏并更新
 * - 数据自动持久化保存到设置
 * 
 * @author VermouthX
 */
public class StockerTableModel extends DefaultTableModel {

    /**
     * 判断单元格是否可编辑
     * 
     * 重写此方法以控制哪些单元格允许用户编辑。
     * 只有成本价（索引4）和持仓数量（索引5）可以编辑。
     * 
     * @param row 行索引
     * @param column 列索引
     * @return true表示可编辑，false表示只读
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        // 只允许编辑成本价（索引4）和持仓数量（索引5）
        // 其他列都是从API获取的实时数据或自动计算的结果，不应手动编辑
        return column == 4 || column == 5;
    }
}
