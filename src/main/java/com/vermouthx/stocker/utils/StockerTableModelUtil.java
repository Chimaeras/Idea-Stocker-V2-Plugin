package com.vermouthx.stocker.utils;

import javax.swing.table.DefaultTableModel;

/**
 * 表格模型工具类
 * 
 * 提供表格数据模型相关的辅助方法。
 * 主要用于在表格中查找特定股票所在的行索引。
 * 
 * 使用场景：
 * 1. 更新股票行情时定位该股票在表格中的位置
 * 2. 删除股票时查找对应的行
 * 3. 检查股票是否已在表格中显示
 * 
 * @author VermouthX
 */
public final class StockerTableModelUtil {
    
    /**
     * 查找股票代码在表格中的行索引
     * 
     * 遍历表格的第一列（股票代码列），查找匹配的股票代码。
     * 用于在更新或删除股票时快速定位其在表格中的位置。
     * 
     * 注意事项：
     * - 第一列（索引0）必须是股票代码列
     * - 使用equals进行精确匹配
     * - 如果有多个相同代码，返回第一个匹配的索引
     * 
     * @param tableModel 表格数据模型
     * @param code 要查找的股票代码
     * @return 股票所在的行索引，未找到时返回-1
     */
    public static int existAt(DefaultTableModel tableModel, String code) {
        // 遍历表格所有行
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            // 获取第一列（股票代码列）的值
            String c = tableModel.getValueAt(i, 0).toString();
            // 精确匹配股票代码
            if (code != null && code.equals(c)) {
                return i;  // 找到匹配，返回行索引
            }
        }
        return -1;  // 未找到，返回-1
    }

    /**
     * 私有构造函数
     * 防止实例化此工具类（所有方法都是静态的）
     */
    private StockerTableModelUtil() {
    }
}
