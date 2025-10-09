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
 * 获取热门股票（成交量排名前十）的工具类
 */
object StockerHotStockUtil {

    private val log = Logger.getInstance(javaClass)

    private val httpClientPool = run {
        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.maxTotal = 20
        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(10000)
            .setSocketTimeout(10000)
            .build()
        HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .useSystemProperties()
            .build()
    }

    /**
     * 获取成交量排名前十的股票代码
     * @param marketType 市场类型
     * @param quoteProvider 数据提供商
     * @return 股票代码列表
     */
    fun getTopVolumeStocks(
        marketType: StockerMarketType,
        quoteProvider: StockerQuoteProvider,
        topN: Int = 10
    ): List<String> {
        return when (marketType) {
            StockerMarketType.AShare -> getTopVolumeAShares(topN)
            StockerMarketType.HKStocks -> getTopVolumeHKStocks(topN)
            StockerMarketType.USStocks -> getTopVolumeUSStocks(topN)
            StockerMarketType.Crypto -> emptyList()
        }
    }

    /**
     * 获取A股成交量前十
     * 使用东方财富网的成交量排行榜API
     */
    private fun getTopVolumeAShares(topN: Int): List<String> {
        return try {
            // 东方财富网成交量排行榜API
            val url = "http://push2.eastmoney.com/api/qt/clist/get?pn=1&pz=$topN&po=1&np=1&fltt=2&invt=2&fid=f5&fs=m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23&fields=f12,f14,f5"
            val httpGet = HttpGet(url)
            
            val response = httpClientPool.execute(httpGet)
            val responseText = EntityUtils.toString(response.entity, "UTF-8")
            
            if (responseText.isBlank()) {
                log.warn("Empty response from volume ranking API")
                return emptyList()
            }
            
            // 解析JSON响应
            parseEastMoneyResponse(responseText)
        } catch (e: Exception) {
            log.warn("Failed to fetch top volume A-shares: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 获取港股成交量前十
     */
    private fun getTopVolumeHKStocks(topN: Int): List<String> {
        // 港股默认返回常见的活跃股票
        return listOf("00700", "09988", "00388", "03690", "00941", "01810", "02318", "01299", "00005", "02015").take(topN)
    }

    /**
     * 获取美股成交量前十
     */
    private fun getTopVolumeUSStocks(topN: Int): List<String> {
        // 美股默认返回常见的活跃股票
        return listOf("AAPL", "TSLA", "NVDA", "MSFT", "AMZN", "GOOGL", "META", "AMD", "NFLX", "BABA").take(topN)
    }

    /**
     * 解析东方财富网的JSON响应
     */
    private fun parseEastMoneyResponse(responseText: String): List<String> {
        return try {
            val codes = mutableListOf<String>()
            
            // 简单的JSON解析：查找 "f12":"代码" 模式
            val regex = Regex(""""f12":"(\w+)"""")
            val matches = regex.findAll(responseText)
            
            matches.forEach { matchResult ->
                val code = matchResult.groupValues[1]
                if (code.isNotEmpty()) {
                    // 添加市场前缀
                    val fullCode = when {
                        code.startsWith("6") -> "sh$code"
                        code.startsWith("0") || code.startsWith("3") -> "sz$code"
                        else -> code
                    }
                    codes.add(fullCode)
                }
            }
            
            codes.take(10)
        } catch (e: Exception) {
            log.warn("Failed to parse volume ranking response: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 获取成交量前十的股票详细信息
     */
    fun getTopVolumeStocksWithDetails(
        marketType: StockerMarketType,
        quoteProvider: StockerQuoteProvider,
        topN: Int = 10
    ): List<StockerQuote> {
        val codes = getTopVolumeStocks(marketType, quoteProvider, topN)
        if (codes.isEmpty()) {
            return emptyList()
        }
        return StockerQuoteHttpUtil.get(marketType, quoteProvider, codes)
    }
}

