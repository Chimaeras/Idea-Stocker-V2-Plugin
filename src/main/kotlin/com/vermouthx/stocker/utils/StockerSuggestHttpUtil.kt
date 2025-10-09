package com.vermouthx.stocker.utils

import com.intellij.openapi.diagnostic.Logger
import com.vermouthx.stocker.entities.StockerSuggestion
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.enums.StockerQuoteProvider
import org.apache.commons.text.StringEscapeUtils
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils

/**
 * 股票搜索建议HTTP请求工具类
 * 
 * 负责向数据提供商请求股票搜索建议。
 * 当用户输入关键词（股票代码或名称）时，自动联想并返回匹配的股票列表。
 * 
 * 主要功能：
 * 1. 根据关键词搜索股票（支持代码和名称）
 * 2. 解析不同提供商的搜索结果格式
 * 3. 自动分类股票所属市场
 * 4. 过滤不支持的股票类型
 * 
 * 支持的搜索方式：
 * - 股票代码（如：600000、00700、AAPL）
 * - 股票名称（如：浦发、腾讯、苹果）
 * - 拼音首字母（如：pfyh）
 * 
 * @author VermouthX
 */
object StockerSuggestHttpUtil {

    /** 日志记录器 */
    private val log = Logger.getInstance(javaClass)

    /**
     * HTTP客户端连接池
     * 配置与StockerQuoteHttpUtil相同，支持高并发搜索请求
     */
    private val httpClientPool = run {
        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.maxTotal = 20
        val requestConfig = RequestConfig.custom().build()
        HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .useSystemProperties()
            .build()
    }

    /**
     * 根据关键词搜索股票建议
     * 
     * 向数据提供商发送搜索请求，获取匹配的股票列表。
     * 支持模糊搜索，可以通过代码、名称或拼音首字母查找。
     * 
     * @param key 搜索关键词（股票代码、名称或拼音）
     * @param provider 数据提供商（新浪或腾讯）
     * @return 匹配的股票建议列表，失败时返回空列表
     */
    fun suggest(key: String, provider: StockerQuoteProvider): List<StockerSuggestion> {
        val url = "${provider.suggestHost}$key"
        val httpGet = HttpGet(url)
        if (provider == StockerQuoteProvider.SINA) {
            httpGet.setHeader("Referer", "https://finance.sina.com.cn") // Sina API requires this header
        }
        return try {
            val response = httpClientPool.execute(httpGet)
            when (provider) {
                StockerQuoteProvider.SINA -> {
                    val responseText = EntityUtils.toString(response.entity, "UTF-8")
                    parseSinaSuggestion(responseText)
                }

                StockerQuoteProvider.TENCENT -> {
                    val responseText = EntityUtils.toString(response.entity, "UTF-8")
                    parseTencentSuggestion(responseText)
                }

            }
        } catch (e: Exception) {
            log.warn(e)
            emptyList()
        }
    }

    private fun parseSinaSuggestion(responseText: String): List<StockerSuggestion> {
        val result = mutableListOf<StockerSuggestion>()
        val regex = Regex("var suggestvalue=\"(.*?)\";")
        val matchResult = regex.find(responseText)
        val (_, snippetsText) = matchResult!!.groupValues
        if (snippetsText.isEmpty()) {
            return emptyList()
        }
        val snippets = snippetsText.split(";")
        for (snippet in snippets) {
            val columns = snippet.split(",")
            if (columns.size < 5) {
                continue
            }
            when (columns[1]) {
                "11" -> {
                    if (columns[4].startsWith("S*ST")) {
                        continue
                    }
                    result.add(StockerSuggestion(columns[3].uppercase(), columns[4], StockerMarketType.AShare))
                }

                "22" -> {
                    val code = columns[3].replace("of", "")
                    when {
                        code.startsWith("15") || code.startsWith("16") || code.startsWith("18") -> result.add(
                            StockerSuggestion("SZ$code", columns[4], StockerMarketType.AShare)
                        )

                        code.startsWith("50") || code.startsWith("51") -> result.add(
                            StockerSuggestion(
                                "SH$code", columns[4], StockerMarketType.AShare
                            )
                        )
                    }
                }

                "31" -> result.add(StockerSuggestion(columns[3].uppercase(), columns[4], StockerMarketType.HKStocks))
                "41" -> result.add(StockerSuggestion(columns[3].uppercase(), columns[4], StockerMarketType.USStocks))
                "71" -> result.add(StockerSuggestion(columns[3].uppercase(), columns[4], StockerMarketType.Crypto))
                "81" -> result.add(StockerSuggestion(columns[3].uppercase(), columns[4], StockerMarketType.AShare))
            }
        }
        return result
    }

    private fun parseTencentSuggestion(responseText: String): List<StockerSuggestion> {
        if (responseText.isEmpty()) {
            return emptyList()
        }
        val result = mutableListOf<StockerSuggestion>()
        val snippets = responseText.replace("v_hint=\"", "").replace("\"", "").split("^")
        for (snippet in snippets) {
            val columns = snippet.split("~")
            if (columns.size < 3) {
                continue
            }
            val type = columns[0]
            val code = columns[1]
            val rawName = columns[2]
            val name = StringEscapeUtils.unescapeJava(rawName)
            when (type) {
                "sz", "sh" -> result.add(StockerSuggestion(type.uppercase() + code, name, StockerMarketType.AShare))

                "hk" -> result.add(StockerSuggestion(code, name, StockerMarketType.HKStocks))

                "us" -> result.add(StockerSuggestion(code.split(".")[0].uppercase(), name, StockerMarketType.USStocks))
            }
        }
        return result
    }
}
