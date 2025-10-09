package com.vermouthx.stocker.enums

/**
 * 股票涨跌颜色方案枚举
 * 
 * 定义股票涨跌时的颜色显示方案。
 * 不同地区有不同的颜色习惯：
 * - 中国大陆：红涨绿跌（R.U.G.D. - Red Up Green Down）
 * - 欧美市场：绿涨红跌（G.U.R.D. - Green Up Red Down）
 * 
 * 该枚举允许用户根据习惯选择颜色方案，提升用户体验。
 * 颜色会应用于表格中的价格、涨跌幅、盈亏等数值列。
 * 
 * 使用场景：
 * 1. 设置界面的颜色方案选项
 * 2. 表格渲染器根据方案设置文字颜色
 * 3. 指数面板的颜色显示
 * 
 * @property title 颜色方案的显示名称，用于设置界面
 * 
 * @author VermouthX
 */
enum class StockerQuoteColorPattern(val title: String) {
    /** 红涨绿跌模式（中国大陆习惯） */
    RED_UP_GREEN_DOWN("R.U.G.D. Mode"),
    
    /** 绿涨红跌模式（欧美市场习惯） */
    GREEN_UP_RED_DOWN("G.U.R.D. Mode"),
    
    /** 无颜色模式（使用默认前景色） */
    NONE("None")
}