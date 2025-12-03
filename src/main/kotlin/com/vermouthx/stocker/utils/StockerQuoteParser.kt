package com.vermouthx.stocker.utils

import com.vermouthx.stocker.entities.StockerQuote
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * 股票行情数据解析器
 * 
 * 负责将从不同数据提供商获取的原始响应文本解析为StockerQuote对象列表。
 * 支持新浪财经和腾讯财经两种数据源，每种数据源有不同的响应格式。
 * 
 * 主要功能：
 * 1. 解析新浪财经的行情数据（JavaScript变量格式）
 * 2. 解析腾讯财经的行情数据（自定义格式）
 * 3. 处理不同市场类型的数据差异
 * 4. 格式化时间和数值
 * 5. 计算涨跌额和涨跌幅
 * 
 * 数据安全：
 * - 所有数组访问前都进行边界检查
 * - 使用mapNotNull过滤无效数据
 * - try-catch捕获解析异常
 * - 避免StringIndexOutOfBoundsException
 * 
 * @author VermouthX
 */
object StockerQuoteParser {

    /**
     * 安全地解析Double值，避免NaN和无效值
     * 
     * 处理各种无效输入情况：
     * - 空字符串 -> 返回默认值
     * - "--" 或 "-" -> 返回默认值
     * - 非数字字符串 -> 返回默认值
     * - 已经是NaN或Infinity的值 -> 返回默认值
     * 
     * @param value 待解析的字符串
     * @param default 解析失败时的默认值，默认为0.0
     * @return 解析后的Double值，失败时返回默认值
     */
    private fun String.safeToDouble(default: Double = 0.0): Double {
        return try {
            // 检查空字符串和常见的无效值
            val trimmed = this.trim()
            if (trimmed.isBlank() || trimmed == "--" || trimmed == "-" || trimmed == "N/A" || trimmed == "null") {
                return default
            }
            
            val result = trimmed.toDouble()
            
            // 检查是否为NaN或Infinity
            if (result.isNaN() || result.isInfinite()) {
                default
            } else {
                result
            }
        } catch (e: NumberFormatException) {
            default
        }
    }

    /**
     * Double扩展函数：保留两位小数，并处理NaN
     * 
     * 通过四舍五入将Double值格式化为保留两位小数。
     * 如果值是NaN或Infinity，返回0.0。
     * 
     * 注意：此方法已废弃，请使用 safeTwoDigits() 方法
     * 
     * @return 保留两位小数的Double值，NaN/Infinity时返回0.0
     */
    @Deprecated("Use safeTwoDigits() instead", ReplaceWith("this.safeTwoDigits()"))
    private fun Double.twoDigits(): Double {
        // 双重检查NaN和Infinity，确保安全
        if (!this.isFinite() || this.isNaN() || this.isInfinite()) {
            return 0.0
        }
        try {
            // 再次检查，防止在计算过程中产生NaN
            val multiplied = this * 100.0
            if (!multiplied.isFinite() || multiplied.isNaN() || multiplied.isInfinite()) {
                return 0.0
            }
            val rounded = multiplied.roundToInt()
            val result = rounded / 100.0
            // 最终检查结果
            if (!result.isFinite() || result.isNaN() || result.isInfinite()) {
                return 0.0
            }
            return result
        } catch (e: Exception) {
            // 捕获任何可能的异常（包括IllegalArgumentException）
            return 0.0
        }
    }
    
    /**
     * 安全的Double扩展函数：保留两位小数，处理NaN
     * 
     * 如果值是NaN或Infinity，返回默认值。
     * 使用双重检查和异常捕获，确保不会在roundToInt时抛出异常。
     * 
     * @param default NaN/Infinity时返回的默认值
     * @return 保留两位小数的Double值
     */
    private fun Double.safeTwoDigits(default: Double = 0.0): Double {
        // 双重检查NaN和Infinity，确保安全
        if (!this.isFinite() || this.isNaN() || this.isInfinite()) {
            return default
        }
        try {
            // 再次检查，防止在计算过程中产生NaN
            val multiplied = this * 100.0
            if (!multiplied.isFinite() || multiplied.isNaN() || multiplied.isInfinite()) {
                return default
            }
            val rounded = multiplied.roundToInt()
            val result = rounded / 100.0
            // 最终检查结果
            if (!result.isFinite() || result.isNaN() || result.isInfinite()) {
                return default
            }
            return result
        } catch (e: Exception) {
            // 捕获任何可能的异常（包括IllegalArgumentException: Cannot round NaN value）
            return default
        }
    }
    
    /**
     * 验证StockerQuote数据有效性
     * 
     * 检查关键字段是否为NaN或无效值。
     * 
     * @param quote 待验证的行情数据
     * @return true表示数据有效，false表示数据无效应被过滤
     */
    private fun validateQuote(quote: StockerQuote): Boolean {
        return !quote.current.isNaN() 
            && !quote.current.isInfinite()
            && !quote.percentage.isNaN()
            && !quote.percentage.isInfinite()
            && quote.current > 0  // 当前价格应该大于0
            && quote.name.isNotBlank()
            && quote.code.isNotBlank()
    }

    /**
     * 解析行情响应的统一入口
     * 
     * 根据不同的数据提供商调用相应的解析方法。
     * 每个提供商的响应格式完全不同，需要专门的解析逻辑。
     * 
     * @param provider 数据提供商（新浪或腾讯）
     * @param marketType 市场类型（A股、港股、美股等）
     * @param responseText HTTP响应的原始文本
     * @return 解析后的股票行情列表，解析失败时返回空列表
     */
    fun parseQuoteResponse(
        provider: StockerQuoteProvider, marketType: StockerMarketType, responseText: String
    ): List<StockerQuote> {
        return when (provider) {
            StockerQuoteProvider.SINA -> parseSinaQuoteResponse(marketType, responseText)
            StockerQuoteProvider.TENCENT -> parseTencentQuoteResponse(marketType, responseText)
        }
    }

    /**
     * 解析新浪财经的行情响应
     * 
     * 新浪财经返回的是JavaScript变量赋值格式：
     * var hq_str_sh600000="浦发银行,8.50,8.45,8.52,...";
     * 
     * 解析流程：
     * 1. 使用正则表达式提取股票代码和行情数据
     * 2. 按逗号分割行情数据
     * 3. 根据市场类型从不同位置提取字段
     * 4. 计算涨跌额和涨跌幅
     * 5. 格式化时间
     * 
     * 异常处理：
     * - 正则匹配失败：跳过该行
     * - 数组长度不足：跳过该条数据
     * - 字符串索引越界：跳过该条数据
     * - 数值解析失败：跳过该条数据
     * 
     * @param marketType 市场类型，决定数据字段的位置
     * @param responseText 新浪API的原始响应文本
     * @return 解析成功的股票行情列表，失败的数据会被过滤掉
     */
    private fun parseSinaQuoteResponse(marketType: StockerMarketType, responseText: String): List<StockerQuote> {
        // 正则表达式：匹配 var hq_str_代码="数据";
        val regex = Regex("var hq_str_(\\w+?)=\"(.*?)\";")
        
        return responseText.split("\n").asSequence()
            .filter { text -> text.isNotEmpty() }  // 过滤空行
            .mapNotNull { text ->
                // 使用正则提取股票代码和行情数据
                val matchResult = regex.find(text) ?: return@mapNotNull null
                val (_, code, quote) = matchResult.groupValues
                "${code},${quote}"  // 组合成：代码,字段1,字段2,...
            }
            .map { text -> text.split(",") }  // 按逗号分割成数组
            .mapNotNull { textArray ->
            try {
                when (marketType) {
                    StockerMarketType.AShare -> {
                        // A股数据格式（新浪）
                        // 数组索引：0=代码, 1=名称, 2=开盘, 3=昨收, 4=现价, 5=最高, 6=最低, 31=日期, 32=时间
                        
                        // 检查数组大小，避免越界
                        if (textArray.size < 33) return@mapNotNull null
                        
                        val code = textArray[0].uppercase()              // 股票代码转大写
                        val name = textArray[1]                          // 股票名称
                        val opening = textArray[2].safeToDouble()        // 今日开盘价
                        val close = textArray[3].safeToDouble()          // 昨日收盘价
                        val current = textArray[4].safeToDouble()        // 当前价格
                        val high = textArray[5].safeToDouble()           // 今日最高价
                        val low = textArray[6].safeToDouble()            // 今日最低价
                        
                        // 在计算前验证所有值的有效性
                        if (!current.isFinite() || !close.isFinite() || 
                            current.isNaN() || close.isNaN() ||
                            current.isInfinite() || close.isInfinite()) {
                            return@mapNotNull null  // 数据无效，跳过这条记录
                        }
                        
                        val change = (current - close).safeTwoDigits()   // 涨跌额 = 现价 - 昨收
                        // 避免除零：如果昨收价为0，涨跌幅设为0
                        val percentage = if (close != 0.0 && close.isFinite() && current.isFinite()) {
                            ((current - close) / close * 100).safeTwoDigits()
                        } else {
                            0.0
                        }
                        val updateAt = textArray[31] + " " + textArray[32]  // 更新时间：日期 + 时间
                        
                        val quote = StockerQuote(
                            code = code,
                            name = name,
                            current = current,
                            opening = opening,
                            close = close,
                            low = low,
                            high = high,
                            change = change,
                            percentage = percentage,
                            updateAt = updateAt
                        )
                        
                        // 验证数据有效性，无效数据返回null被过滤
                        if (validateQuote(quote)) quote else null
                    }

                    StockerMarketType.HKStocks -> {
                        // 港股数据格式（新浪）
                        // 数组索引：0=hk代码, 2=名称, 3=开盘, 4=昨收, 5=最高, 6=最低, 7=现价, 9=涨跌幅, 18=日期, 19=时间
                        
                        // 检查数组大小和字符串长度，避免越界
                        if (textArray.size < 20 || textArray[0].length < 2) return@mapNotNull null
                        
                        val code = textArray[0].substring(2).uppercase()  // 去掉前缀"hk"，提取股票代码
                        val name = textArray[2]                           // 股票名称
                        val opening = textArray[3].safeToDouble()         // 今日开盘价
                        val close = textArray[4].safeToDouble()           // 昨日收盘价
                        val high = textArray[5].safeToDouble()            // 今日最高价
                        val low = textArray[6].safeToDouble()             // 今日最低价
                        val current = textArray[7].safeToDouble()         // 当前价格
                        val percentageRaw = textArray[9].safeToDouble()   // 涨跌幅（已计算好）
                        
                        // 在计算前验证所有值的有效性
                        if (!current.isFinite() || !close.isFinite() || !percentageRaw.isFinite() ||
                            current.isNaN() || close.isNaN() || percentageRaw.isNaN() ||
                            current.isInfinite() || close.isInfinite() || percentageRaw.isInfinite()) {
                            return@mapNotNull null  // 数据无效，跳过这条记录
                        }
                        
                        val change = (current - close).safeTwoDigits()    // 涨跌额
                        val percentage = percentageRaw.safeTwoDigits()     // 涨跌幅
                        
                        // 时间格式转换：yyyy/MM/dd HH:mm -> yyyy-MM-dd HH:mm:ss
                        val sourceFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                        val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        val datetime = LocalDateTime.parse(textArray[18] + " " + textArray[19], sourceFormatter)
                        val updateAt = targetFormatter.format(datetime)
                        
                        val quote = StockerQuote(
                            code = code,
                            name = name,
                            current = current,
                            opening = opening,
                            close = close,
                            low = low,
                            high = high,
                            change = change,
                            percentage = percentage,
                            updateAt = updateAt
                        )
                        
                        // 验证数据有效性
                        if (validateQuote(quote)) quote else null
                    }

                    StockerMarketType.USStocks -> {
                        // 美股数据格式（新浪）
                        // 数组索引：0=gb_代码, 1=名称, 2=现价, 3=涨跌幅, 4=更新时间, 6=开盘, 7=最高, 8=最低, 27=昨收
                        
                        // 检查数组大小和字符串长度，避免越界
                        if (textArray.size < 28 || textArray[0].length < 3) return@mapNotNull null
                        
                        val code = textArray[0].substring(3).uppercase()  // 去掉前缀"gb_"，提取股票代码
                        val name = textArray[1]                           // 股票名称
                        val current = textArray[2].safeToDouble()         // 当前价格
                        val updateAt = textArray[4]                       // 更新时间（已格式化）
                        val opening = textArray[6].safeToDouble()         // 今日开盘价
                        val high = textArray[7].safeToDouble()            // 今日最高价
                        val low = textArray[8].safeToDouble()             // 今日最低价
                        val close = textArray[27].safeToDouble()          // 昨日收盘价
                        val percentageRaw = textArray[3].safeToDouble()   // 涨跌幅（已计算好）
                        
                        // 在计算前验证所有值的有效性
                        if (!current.isFinite() || !close.isFinite() || !percentageRaw.isFinite() ||
                            current.isNaN() || close.isNaN() || percentageRaw.isNaN() ||
                            current.isInfinite() || close.isInfinite() || percentageRaw.isInfinite()) {
                            return@mapNotNull null  // 数据无效，跳过这条记录
                        }
                        
                        val change = (current - close).safeTwoDigits()    // 涨跌额
                        val percentage = percentageRaw.safeTwoDigits()     // 涨跌幅
                        
                        val quote = StockerQuote(
                            code = code,
                            name = name,
                            current = current,
                            opening = opening,
                            close = close,
                            low = low,
                            high = high,
                            change = change,
                            percentage = percentage,
                            updateAt = updateAt
                        )
                        
                        // 验证数据有效性
                        if (validateQuote(quote)) quote else null
                    }

                    StockerMarketType.Crypto -> {
                        // 加密货币数据格式（新浪）
                        // 数组索引：0=btc_代码, 1=时间, 6=开盘, 7=最高, 8=最低, 9=现价, 10=名称, 12=日期
                        // 注意：加密货币没有昨收价，使用开盘价作为基准计算涨跌
                        
                        // 检查数组大小和字符串长度，避免越界
                        if (textArray.size < 13 || textArray[0].length < 4) return@mapNotNull null
                        
                        val code = textArray[0].substring(4).uppercase()  // 去掉前缀"btc_"，提取代码
                        val name = textArray[10]                          // 加密货币名称
                        val current = textArray[9].safeToDouble()         // 当前价格
                        val low = textArray[8].safeToDouble()             // 今日最低价
                        val high = textArray[7].safeToDouble()            // 今日最高价
                        val opening = textArray[6].safeToDouble()         // 今日开盘价
                        
                        // 在计算前验证所有值的有效性
                        if (!current.isFinite() || !opening.isFinite() ||
                            current.isNaN() || opening.isNaN() ||
                            current.isInfinite() || opening.isInfinite()) {
                            return@mapNotNull null  // 数据无效，跳过这条记录
                        }
                        
                        val change = (current - opening).safeTwoDigits()  // 涨跌额（相对开盘价）
                        // 避免除零：如果开盘价为0，涨跌幅设为0
                        val percentage = if (opening != 0.0 && opening.isFinite() && current.isFinite()) {
                            ((current - opening) / opening * 100).safeTwoDigits()
                        } else {
                            0.0
                        }
                        val updateAt = "${textArray[12]} ${textArray[1]}" // 更新时间：日期 + 时间
                        
                        val quote = StockerQuote(
                            code = code,
                            name = name,
                            current = current,
                            opening = opening,
                            close = current,
                            low = low,
                            high = high,
                            change = change,
                            percentage = percentage,
                            updateAt = updateAt
                        )
                        
                        // 验证数据有效性
                        if (validateQuote(quote)) quote else null
                    }
                }
            } catch (e: Exception) {
                null
            }
        }.toList()
    }

    /**
     * 解析腾讯财经的行情响应
     * 
     * 腾讯财经返回的格式：
     * v_sh600000="1~浦发银行~600000~8.50~2.5~8.45~8.52~..."
     * 
     * 解析流程：
     * 1. 定位'='和双引号的位置
     * 2. 提取股票代码（等号前）
     * 3. 提取行情数据（引号内）
     * 4. 按波浪号分割数据
     * 5. 根据市场类型从不同位置提取字段
     * 
     * 异常处理：
     * - 字符查找失败：跳过该行
     * - 数组长度不足：跳过该条数据
     * - 数值解析失败：跳过该条数据
     * 
     * @param marketType 市场类型
     * @param responseText 腾讯API的原始响应文本
     * @return 解析成功的股票行情列表
     */
    private fun parseTencentQuoteResponse(marketType: StockerMarketType, responseText: String): List<StockerQuote> {
        return responseText.split("\n").asSequence()
            .filter { text -> text.isNotEmpty() }  // 过滤空行
            .mapNotNull { text ->
            // 查找关键字符的位置：等号和双引号
            val equalIndex = text.indexOfFirst { c -> c == '=' }
            val firstQuoteIndex = text.indexOfFirst { c -> c == '"' }
            val lastQuoteIndex = text.indexOfLast { c -> c == '"' }
            
            // 检查是否找到了必要的字符，避免 StringIndexOutOfBoundsException
            if (equalIndex == -1 || firstQuoteIndex == -1 || lastQuoteIndex == -1 || firstQuoteIndex >= lastQuoteIndex) {
                return@mapNotNull null
            }
            
            // 提取股票代码（根据市场类型，代码在等号前不同位置）
            val code = when (marketType) {
                StockerMarketType.AShare -> {
                    // A股格式：v_sh600000=... 从索引2开始到等号
                    if (equalIndex < 2) return@mapNotNull null
                    text.subSequence(2, equalIndex)
                }
                StockerMarketType.HKStocks, StockerMarketType.USStocks -> {
                    // 港股/美股格式：v_hk00700=... 或 v_us.aapl=... 从索引4开始到等号
                    if (equalIndex < 4) return@mapNotNull null
                    text.subSequence(4, equalIndex)
                }
                StockerMarketType.Crypto -> ""
            }
            // 组合：代码~行情数据
            "$code~${text.subSequence(firstQuoteIndex + 1, lastQuoteIndex)}"
        }.map { text -> text.split("~") }  // 按波浪号分割
          .mapNotNull { textArray ->
            // 腾讯数据格式（通用）
            // 数组索引：0=代码, 2=名称, 4=现价, 5=昨收, 6=开盘, 31=时间, 33=涨跌幅, 34=最高, 35=最低
            
            // 检查数组是否有足够的元素，避免数组越界
            if (textArray.size < 36) {
                return@mapNotNull null
            }
            
            try {
                val code = textArray[0].uppercase()              // 股票代码
                val name = textArray[2]                          // 股票名称
                val opening = textArray[6].safeToDouble()        // 今日开盘价
                val close = textArray[5].safeToDouble()          // 昨日收盘价
                val current = textArray[4].safeToDouble()        // 当前价格
                val high = textArray[34].safeToDouble()          // 今日最高价
                val low = textArray[35].safeToDouble()           // 今日最低价
                val percentageRaw = textArray[33].safeToDouble() // 涨跌幅
                
                // 在计算前验证所有值的有效性
                if (!current.isFinite() || !close.isFinite() || !percentageRaw.isFinite() ||
                    current.isNaN() || close.isNaN() || percentageRaw.isNaN() ||
                    current.isInfinite() || close.isInfinite() || percentageRaw.isInfinite()) {
                    return@mapNotNull null  // 数据无效，跳过这条记录
                }
                
                val change = (current - close).safeTwoDigits()   // 涨跌额
                val percentage = percentageRaw.safeTwoDigits()    // 涨跌幅
                
                // 根据不同市场类型格式化更新时间
                val updateAt = when (marketType) {
                    StockerMarketType.AShare -> {
                        // A股时间格式：20250109153000 -> 2025-01-09 15:30:00
                        val sourceFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                        val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        val datetime = LocalDateTime.parse(textArray[31], sourceFormatter)
                        targetFormatter.format(datetime)
                    }

                    StockerMarketType.HKStocks -> {
                        // 港股时间格式：2025/01/09 15:30:00 -> 2025-01-09 15:30:00
                        val sourceFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                        val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        val datetime = LocalDateTime.parse(textArray[31], sourceFormatter)
                        targetFormatter.format(datetime)
                    }

                    StockerMarketType.USStocks -> textArray[31]  // 美股时间已经是标准格式
                    StockerMarketType.Crypto -> ""               // 加密货币暂不支持
                }
                val quote = StockerQuote(
                    code = code,
                    name = name,
                    current = current,
                    opening = opening,
                    close = close,
                    low = low,
                    high = high,
                    change = change,
                    percentage = percentage,
                    updateAt = updateAt
                )
                
                // 验证数据有效性
                if (validateQuote(quote)) quote else null
            } catch (e: Exception) {
                null
            }
        }.toList()
    }
}
