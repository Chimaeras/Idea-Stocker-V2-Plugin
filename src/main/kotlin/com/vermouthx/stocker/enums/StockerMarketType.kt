package com.vermouthx.stocker.enums

/**
 * 股票市场类型枚举
 * 
 * 定义插件支持的所有股票市场类型。
 * 不同市场有不同的数据格式、API接口和交易规则，
 * 该枚举用于区分处理逻辑和数据解析方式。
 * 
 * 使用场景：
 * 1. 选择数据提供商的API端点
 * 2. 区分不同市场的数据解析逻辑
 * 3. 管理不同市场的收藏列表
 * 4. 设置界面的市场分类
 * 
 * @property title 市场的简称标识，用于UI显示和配置
 * 
 * @author VermouthX
 */
enum class StockerMarketType(val title: String) {
    /** A股市场（中国大陆股市：上海、深圳） */
    AShare("CN"),
    
    /** 港股市场（香港联合交易所） */
    HKStocks("HK"),
    
    /** 美股市场（纽交所、纳斯达克） */
    USStocks("US"),
    
    /** 加密货币市场（比特币等数字货币） */
    Crypto("Crypto")
}