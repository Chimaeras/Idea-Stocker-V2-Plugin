package com.vermouthx.stocker.enums;

/**
 * 股票操作类型枚举
 * 
 * 定义对股票的操作类型，主要用于股票搜索对话框中的按钮显示和行为控制。
 * 根据股票是否已被收藏，按钮会显示不同的操作文本（Add/Delete）。
 * 
 * 使用场景：
 * 1. 股票搜索对话框中的操作按钮文本
 * 2. 根据按钮文本判断要执行的操作类型
 * 3. 动态切换按钮状态（添加 <-> 删除）
 * 
 * @author VermouthX
 */
public enum StockerStockOperation {
    /** 添加股票到收藏列表 */
    STOCK_ADD("Add"),
    
    /** 从收藏列表删除股票 */
    STOCK_DELETE("Delete");

    /** 操作类型的文本标识 */
    private final String operation;

    /**
     * 构造函数
     * 
     * @param operation 操作类型的文本标识
     */
    StockerStockOperation(String operation) {
        this.operation = operation;
    }

    /**
     * 根据操作文本获取对应的枚举值
     * 
     * 用于从按钮文本反向查找操作类型，以执行相应的添加或删除逻辑。
     * 
     * @param des 操作文本（"Add" 或 "Delete"）
     * @return 对应的StockerStockOperation枚举值，默认返回DELETE
     */
    public static StockerStockOperation mapOf(String des) {
        if ("Add".equals(des)) {
            return STOCK_ADD;
        }
        return STOCK_DELETE;
    }

    /**
     * 获取操作类型的文本标识
     * 
     * @return 操作文本（"Add" 或 "Delete"）
     */
    public String getOperation() {
        return operation;
    }
}
