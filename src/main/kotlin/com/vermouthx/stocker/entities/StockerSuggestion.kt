package com.vermouthx.stocker.entities

import com.vermouthx.stocker.enums.StockerMarketType

/**
 * 股票搜索建议实体类
 * 
 * 用于存储搜索股票时返回的建议项数据。
 * 当用户在搜索框输入关键词时，会从数据提供商获取相关的股票建议，
 * 每个建议包含股票代码、名称和所属市场信息。
 * 
 * 使用场景：
 * 1. 股票搜索对话框的自动完成功能
 * 2. 添加新股票时的搜索结果展示
 * 3. 股票代码验证和补全
 * 
 * @property code 股票代码（如：sh600000, 00700, AAPL）
 * @property name 股票名称（如：浦发银行、腾讯控股、苹果公司）
 * @property market 所属市场类型（A股、港股、美股或加密货币）
 * 
 * @author VermouthX
 */
data class StockerSuggestion(
    val code: String, 
    val name: String, 
    val market: StockerMarketType
)
