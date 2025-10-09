package com.vermouthx.stocker.entities

/**
 * 股票行情数据实体类
 * 
 * 用于存储单个股票的实时行情信息，包括价格、涨跌幅等关键数据。
 * 该实体类被用于表格显示、数据传输和缓存等场景。
 * 
 * @property code 股票代码（如：sh600000, 00700, AAPL）
 * @property name 股票名称（如：浦发银行、腾讯控股、苹果）
 * @property current 当前价格（实时价格）
 * @property opening 开盘价
 * @property close 昨日收盘价（用于计算涨跌幅）
 * @property low 当日最低价
 * @property high 当日最高价
 * @property change 涨跌额（当前价格 - 昨收价）
 * @property percentage 涨跌幅百分比
 * @property buys 买盘价格数组（预留字段，暂未使用）
 * @property sells 卖盘价格数组（预留字段，暂未使用）
 * @property updateAt 数据更新时间（格式：yyyy-MM-dd HH:mm:ss）
 * 
 * @author VermouthX
 */
data class StockerQuote(
    var code: String,
    var name: String,
    var current: Double,
    var opening: Double,
    var close: Double,
    var low: Double,
    var high: Double,
    var change: Double,
    var percentage: Double,
    var buys: Array<Double> = emptyArray(),
    var sells: Array<Double> = emptyArray(),
    var updateAt: String
) {
    /**
     * 重写equals方法
     * 两个StockerQuote对象只要股票代码相同就视为相等
     * 这样可以方便地去重和查找
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StockerQuote

        return code == other.code
    }

    /**
     * 重写hashCode方法
     * 与equals保持一致，使用股票代码的哈希值
     * 确保在Set和Map等集合中正常工作
     */
    override fun hashCode(): Int {
        return code.hashCode()
    }
}
