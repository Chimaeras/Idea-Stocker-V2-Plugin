package com.vermouthx.stocker.enums

/**
 * 市场指数枚举
 * 
 * 定义各个市场的主要指数代码列表。
 * 这些指数用于展示市场整体走势，显示在工具窗口底部的指数面板中。
 * 用户可以通过下拉框选择不同的指数查看其实时数据。
 * 
 * 使用场景：
 * 1. 获取并显示市场指数的实时行情
 * 2. 为用户提供市场大盘参考
 * 3. 在工具窗口底部展示指数信息
 * 
 * @property codes 指数代码列表，用于从API获取指数行情数据
 * 
 * @author VermouthX
 */
enum class StockerMarketIndex(val codes: List<String>) {
    /** 
     * 中国市场指数
     * - SH000001: 上证指数（上海证券交易所综合指数）
     * - SZ399001: 深证成指（深圳证券交易所成份指数）
     * - SZ399006: 创业板指（创业板综合指数）
     */
    CN(listOf("SH000001", "SZ399001", "SZ399006")),
    
    /** 
     * 香港市场指数
     * - HSI: 恒生指数（香港股市主要指数）
     */
    HK(listOf("HSI")),
    
    /** 
     * 美国市场指数
     * - DJI: 道琼斯工业平均指数
     * - IXIC: 纳斯达克综合指数
     * - INX: 标普500指数
     */
    US(listOf("DJI", "IXIC", "INX")),
    
    /** 
     * 加密货币市场指数
     * - BTCBTCUSD: 比特币兑美元
     */
    Crypto(listOf("BTCBTCUSD"))
}
