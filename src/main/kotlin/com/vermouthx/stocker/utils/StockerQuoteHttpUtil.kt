package com.vermouthx.stocker.utils

import com.intellij.openapi.diagnostic.Logger
import com.vermouthx.stocker.entities.StockerQuote
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteProvider
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils

/**
 * 股票行情HTTP请求工具类
 * 
 * 负责从不同的数据提供商（新浪、腾讯）获取股票实时行情数据。
 * 使用HTTP连接池提升性能，支持并发请求。
 * 
 * 主要功能：
 * 1. 批量获取股票行情数据
 * 2. 验证股票代码是否有效
 * 3. 处理不同市场和提供商的API差异
 * 4. 统一的异常处理和错误恢复
 * 
 * 性能优化：
 * - 使用连接池复用HTTP连接
 * - 支持批量请求多个股票
 * - 区分不同类型的网络异常
 * 
 * @author VermouthX
 */
object StockerQuoteHttpUtil {

    /** 日志记录器，用于记录HTTP请求的成功、失败和异常信息 */
    private val log = Logger.getInstance(javaClass)

    /**
     * HTTP客户端连接池
     * 
     * 配置说明：
     * - 最大连接数：20个并发连接
     * - 连接管理器：PoolingHttpClientConnectionManager（支持连接复用）
     * - 系统代理：自动使用系统代理设置
     * 
     * 优势：
     * - 避免频繁创建销毁连接
     * - 提升请求性能
     * - 支持并发请求
     */
    private val httpClientPool = run {
        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.maxTotal = 20  // 最大连接数
        val requestConfig = RequestConfig.custom().build()
        HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .useSystemProperties()  // 使用系统代理配置
            .build()
    }

    /**
     * 批量获取股票行情数据
     * 
     * 根据指定的市场类型和数据提供商，批量获取多个股票的实时行情。
     * 会自动处理股票代码的大小写转换和前缀添加。
     * 
     * 请求流程：
     * 1. 根据提供商和市场类型构造股票代码参数
     * 2. 拼接完整的API URL
     * 3. 发送HTTP GET请求
     * 4. 检查响应状态码
     * 5. 解析响应文本为StockerQuote对象列表
     * 
     * 异常处理：
     * - 空列表：直接返回空列表
     * - 网络超时：记录日志并返回空列表
     * - 网络不可用：记录日志并返回空列表
     * - 响应错误：记录日志并返回空列表
     * - 解析失败：记录日志并返回空列表
     * 
     * @param marketType 市场类型（A股、港股、美股等）
     * @param quoteProvider 数据提供商（新浪或腾讯）
     * @param codes 股票代码列表（不含前缀，如：["600000", "000001"]）
     * @return 股票行情列表，失败时返回空列表
     */
    fun get(
        marketType: StockerMarketType, quoteProvider: StockerQuoteProvider, codes: List<String>
    ): List<StockerQuote> {
        // 空列表检查：避免无效请求
        if (codes.isEmpty()) {
            return emptyList()
        }
        
        return try {
            // 构造股票代码参数：根据提供商和市场类型添加前缀并转换大小写
            // 示例：["600000", "000001"] -> "sh600000,sz000001"（新浪A股）
            val codesParam = when (quoteProvider) {
                StockerQuoteProvider.SINA -> {
                    // 新浪财经的代码规则
                    if (marketType == StockerMarketType.HKStocks) {
                        // 港股：代码转大写，添加"hk"前缀
                        // 示例："00700" -> "hk00700"
                        codes.joinToString(",") { code ->
                            "${quoteProvider.providerPrefixMap[marketType]}${code.uppercase()}"
                        }
                    } else {
                        // A股/美股/加密货币：代码转小写，添加相应前缀
                        // 示例："600000" -> "sh600000", "AAPL" -> "gb_aapl"
                        codes.joinToString(",") { code ->
                            "${quoteProvider.providerPrefixMap[marketType]}${code.lowercase()}"
                        }
                    }
                }

                StockerQuoteProvider.TENCENT -> {
                    // 腾讯财经的代码规则
                    if (marketType == StockerMarketType.HKStocks || marketType == StockerMarketType.USStocks) {
                        // 港股/美股：代码转大写，添加前缀
                        // 示例："00700" -> "hk00700", "AAPL" -> "usAAPL"
                        codes.joinToString(",") { code ->
                            "${quoteProvider.providerPrefixMap[marketType]}${code.uppercase()}"
                        }
                    } else {
                        // A股：代码转小写，无前缀
                        // 示例："600000" -> "sh600000"
                        codes.joinToString(",") { code ->
                            "${quoteProvider.providerPrefixMap[marketType]}${code.lowercase()}"
                        }
                    }
                }
            }

            // 拼接完整的API URL
            val url = "${quoteProvider.host}${codesParam}"
            
            // 创建HTTP GET请求
            val httpGet = HttpGet(url)
            
            // 新浪API需要设置Referer头，否则会返回403错误
            if (quoteProvider == StockerQuoteProvider.SINA) {
                httpGet.setHeader("Referer", "https://finance.sina.com.cn")
            }
            
            // 执行HTTP请求
            val response = httpClientPool.execute(httpGet)
            val statusCode = response.statusLine.statusCode
            
            // 检查HTTP状态码，非200表示请求失败
            if (statusCode != 200) {
                log.warn("HTTP request failed with status code: $statusCode for $url")
                return emptyList()
            }
            
            // 读取响应内容（UTF-8编码，支持中文）
            val responseText = EntityUtils.toString(response.entity, "UTF-8")
            
            // 检查响应内容是否为空
            if (responseText.isBlank()) {
                log.warn("Empty response from $url")
                return emptyList()
            }
            
            // 调用解析器解析响应文本为StockerQuote对象列表
            StockerQuoteParser.parseQuoteResponse(quoteProvider, marketType, responseText)
            
        } catch (e: java.net.SocketTimeoutException) {
            // 网络超时异常（连接超时或读取超时）
            log.warn("Request timeout for $marketType quotes: ${e.message}")
            emptyList()
        } catch (e: java.net.UnknownHostException) {
            // 网络不可用异常（无法解析主机名，通常是断网）
            log.warn("Network unavailable for $marketType quotes: ${e.message}")
            emptyList()
        } catch (e: Exception) {
            // 其他异常（IO异常、解析异常等）
            log.warn("Failed to fetch $marketType quotes: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 验证股票代码是否有效
     * 
     * 通过向数据提供商API发送请求，检查股票代码是否存在且有效。
     * 不同提供商有不同的验证方式：
     * - 新浪：检查返回数据是否包含逗号（有效数据至少包含一个字段分隔符）
     * - 腾讯：检查响应是否为"v_pv_none_match"（无匹配标识）
     * 
     * 使用场景：
     * 1. 用户添加新股票前验证代码有效性
     * 2. 防止添加无效或已退市的股票
     * 3. 提供友好的错误提示
     * 
     * @param marketType 市场类型
     * @param quoteProvider 数据提供商
     * @param code 待验证的股票代码
     * @return true表示代码有效，false表示无效
     */
    fun validateCode(
        marketType: StockerMarketType, quoteProvider: StockerQuoteProvider, code: String
    ): Boolean {
        when (quoteProvider) {
            StockerQuoteProvider.SINA -> {
                // 构造验证URL
                val url = if (marketType == StockerMarketType.HKStocks) {
                    "${quoteProvider.host}${quoteProvider.providerPrefixMap[marketType]}${code.uppercase()}"
                } else {
                    "${quoteProvider.host}${quoteProvider.providerPrefixMap[marketType]}${code.lowercase()}"
                }
                
                val httpGet = HttpGet(url)
                httpGet.setHeader("Referer", "https://finance.sina.com.cn")  // 新浪API必需的请求头
                
                val response = httpClientPool.execute(httpGet)
                val responseText = EntityUtils.toString(response.entity, "UTF-8")
                val firstLine = responseText.split("\n")[0]
                
                // 查找双引号位置
                val startIndex = firstLine.indexOfFirst { c -> c == '"' }
                val endIndex = firstLine.indexOfLast { c -> c == '"' }
                
                // 检查是否找到了双引号，避免 StringIndexOutOfBoundsException
                if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
                    return false  // 格式不正确，代码无效
                }
                
                val start = startIndex + 1
                val end = endIndex
                if (start == end) {
                    return false  // 引号内为空，代码无效
                }
                
                // 检查引号内是否包含逗号
                // 有效的股票数据至少包含一个字段分隔符（逗号）
                return firstLine.subSequence(start, end).contains(",")
            }

            StockerQuoteProvider.TENCENT -> {
                // 构造验证URL
                val url = if (marketType == StockerMarketType.HKStocks || marketType == StockerMarketType.USStocks) {
                    "${quoteProvider.host}${quoteProvider.providerPrefixMap[marketType]}${code.uppercase()}"
                } else {
                    "${quoteProvider.host}${quoteProvider.providerPrefixMap[marketType]}${code.lowercase()}"
                }
                
                val httpGet = HttpGet(url)
                val response = httpClientPool.execute(httpGet)
                val responseText = EntityUtils.toString(response.entity, "UTF-8")
                
                // 腾讯API返回"v_pv_none_match"表示无匹配结果
                return !responseText.startsWith("v_pv_none_match")
            }
        }
    }
}
