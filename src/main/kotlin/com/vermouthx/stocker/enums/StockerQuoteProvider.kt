package com.vermouthx.stocker.enums

/**
 * 股票数据提供商枚举
 * 
 * 定义插件支持的股票行情数据源。
 * 每个数据提供商有不同的API接口、数据格式和访问规则。
 * 用户可以在设置中选择首选的数据提供商。
 * 
 * 主要功能：
 * 1. 提供行情数据API的URL配置
 * 2. 提供股票搜索建议API的URL配置
 * 3. 定义不同市场的股票代码前缀规则
 * 
 * @property title 提供商显示名称，用于设置界面
 * @property host 行情数据API的基础URL
 * @property suggestHost 股票搜索建议API的基础URL
 * @property providerPrefixMap 各市场类型对应的股票代码前缀映射
 * 
 * @author VermouthX
 */
enum class StockerQuoteProvider(
    val title: String, 
    val host: String, 
    val suggestHost: String, 
    val providerPrefixMap: Map<StockerMarketType, String>
) {
    /**
     * 新浪财经数据源
     * 
     * 特点：
     * - 数据更新快，延迟低
     * - 需要设置Referer头才能访问
     * - 支持A股、港股、美股、加密货币
     * 
     * API示例：
     * - 行情：https://hq.sinajs.cn/list=sh600000,sz000001
     * - 搜索：https://suggest3.sinajs.cn/suggest/key=浦发
     */
    SINA(
        title = "Sina",
        host = "https://hq.sinajs.cn/list=",
        suggestHost = "https://suggest3.sinajs.cn/suggest/key=",
        providerPrefixMap = mapOf(
            StockerMarketType.AShare to "",         // A股无前缀，直接使用sh/sz开头的代码
            StockerMarketType.HKStocks to "hk",     // 港股前缀：hk
            StockerMarketType.USStocks to "gb_",    // 美股前缀：gb_
            StockerMarketType.Crypto to "btc_"      // 加密货币前缀：btc_
        )
    ),

    /**
     * 腾讯财经数据源
     * 
     * 特点：
     * - 数据稳定可靠
     * - 无需特殊请求头
     * - 支持A股、港股、美股
     * 
     * API示例：
     * - 行情：https://qt.gtimg.cn/q=sh600000,sz000001
     * - 搜索：https://smartbox.gtimg.cn/s3/?v=2&t=all&c=1&q=浦发
     */
    TENCENT(
        title = "Tencent",
        host = "https://qt.gtimg.cn/q=",
        suggestHost = "https://smartbox.gtimg.cn/s3/?v=2&t=all&c=1&q=",
        providerPrefixMap = mapOf(
            StockerMarketType.AShare to "",         // A股无前缀
            StockerMarketType.HKStocks to "hk",     // 港股前缀：hk
            StockerMarketType.USStocks to "us",     // 美股前缀：us
        )
    );

    /**
     * 根据标题获取对应的数据提供商
     * 
     * @param title 提供商标题
     * @return 对应的StockerQuoteProvider枚举值，默认返回SINA
     */
    fun fromTitle(title: String): StockerQuoteProvider {
        return when (title) {
            SINA.title -> SINA
            TENCENT.title -> TENCENT
            else -> SINA  // 默认使用新浪数据源
        }
    }

}
