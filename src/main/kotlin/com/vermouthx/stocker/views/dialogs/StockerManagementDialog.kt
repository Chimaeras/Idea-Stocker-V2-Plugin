package com.vermouthx.stocker.views.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.panel
import com.vermouthx.stocker.StockerAppManager
import com.vermouthx.stocker.entities.StockerQuote
import com.vermouthx.stocker.enums.StockerMarketType
import com.vermouthx.stocker.settings.StockerSetting
import com.vermouthx.stocker.utils.StockerQuoteHttpUtil
import com.vermouthx.stocker.utils.StockerHotStockUtil
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.*

class StockerManagementDialog(val project: Project?) : DialogWrapper(project) {

    private val setting = StockerSetting.instance

    private val tabMap: MutableMap<Int, JPanel> = mutableMapOf()

    private val currentSymbols: MutableMap<StockerMarketType, DefaultListModel<StockerQuote>> = mutableMapOf()

    private var currentMarketSelection: StockerMarketType = StockerMarketType.AShare

    init {
        title = "Manage Favorite Stocks"
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        val tabbedPane = JBTabbedPane()
        tabbedPane.add("CN", createTabContent(0))
        tabbedPane.add("HK", createTabContent(1))
        tabbedPane.add("US", createTabContent(2))
//        tabbedPane.add("Crypto", createTabContent(3))
        tabbedPane.addChangeListener {
            currentMarketSelection = when (tabbedPane.selectedIndex) {
                0 -> {
                    StockerMarketType.AShare
                }

                1 -> {
                    StockerMarketType.HKStocks
                }

                2 -> {
                    StockerMarketType.USStocks
                }
//                3 -> {
//                    StockerMarketType.Crypto
//                }
                else -> return@addChangeListener
            }
        }

        val aShareListModel = DefaultListModel<StockerQuote>()
        aShareListModel.addAll(
            StockerQuoteHttpUtil.get(
                StockerMarketType.AShare, setting.quoteProvider, setting.aShareList
            )
        )
        currentSymbols[StockerMarketType.AShare] = aShareListModel
        tabMap[0]?.let { pane ->
            renderTabPane(pane, aShareListModel)
        }

        val hkStocksListModel = DefaultListModel<StockerQuote>()
        hkStocksListModel.addAll(
            StockerQuoteHttpUtil.get(
                StockerMarketType.HKStocks, setting.quoteProvider, setting.hkStocksList
            )
        )
        currentSymbols[StockerMarketType.HKStocks] = hkStocksListModel
        tabMap[1]?.let { pane ->
            renderTabPane(pane, hkStocksListModel)
        }

        val usStocksListModel = DefaultListModel<StockerQuote>()
        usStocksListModel.addAll(
            StockerQuoteHttpUtil.get(
                StockerMarketType.USStocks, setting.quoteProvider, setting.usStocksList
            )
        )
        currentSymbols[StockerMarketType.USStocks] = usStocksListModel
        tabMap[2]?.let { pane ->
            renderTabPane(pane, usStocksListModel)
        }

        tabbedPane.selectedIndex = 0
        return panel {
            row {
                cell(tabbedPane).align(AlignX.FILL)
            }
        }.withPreferredWidth(300)
    }

    override fun createActions(): Array<Action> {
        return arrayOf(
            object : OkAction() {
                override fun actionPerformed(e: ActionEvent?) {
                    val myApplication = StockerAppManager.myApplication(project)
                    if (myApplication != null) {
                        myApplication.shutdownThenClear()
                        currentSymbols[StockerMarketType.AShare]?.let { symbols ->
                            setting.aShareList = symbols.elements().asSequence().map { it.code }.toMutableList()
                        }
                        currentSymbols[StockerMarketType.HKStocks]?.let { symbols ->
                            setting.hkStocksList = symbols.elements().asSequence().map { it.code }.toMutableList()
                        }
                        currentSymbols[StockerMarketType.USStocks]?.let { symbols ->
                            setting.usStocksList = symbols.elements().asSequence().map { it.code }.toMutableList()
                        }
                        myApplication.schedule()
                    }
                    super.actionPerformed(e)
                }
            }, cancelAction
        )
    }

    private fun createTabContent(index: Int): JComponent {
        val pane = JPanel(BorderLayout())
        tabMap[index] = pane
        return panel {
            row {
                cell(pane).align(AlignX.FILL).align(AlignY.FILL)
            }
        }
    }

    private fun renderTabPane(pane: JPanel, listModel: DefaultListModel<StockerQuote>) {
        val list = JBList(listModel)
        val decorator = ToolbarDecorator.createDecorator(list)
            .setAddAction { 
                // 显示选择对话框：手动添加 或 批量添加TOP10
                showAddOptionsDialog(listModel)
            }
        val toolbarPane = decorator.createPanel()
        list.installCellRenderer { symbol ->
            panel {
                row {
                    label(symbol.code).align(AlignX.LEFT)
                    label(
                        if (symbol.name.length <= 20) {
                            symbol.name
                        } else {
                            "${symbol.name.substring(0, 20)}..."
                        }
                    ).align(AlignX.CENTER)
                }
            }.withBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16))
        }
        val scrollPane = JBScrollPane(list)
        pane.add(toolbarPane, BorderLayout.NORTH)
        pane.add(scrollPane, BorderLayout.CENTER)
    }

    /**
     * 显示添加选项对话框
     */
    private fun showAddOptionsDialog(listModel: DefaultListModel<StockerQuote>) {
        val options = arrayOf("手动添加股票", "批量添加成交量TOP10", "取消")
        val result = Messages.showDialog(
            project,
            "请选择添加方式",
            "添加股票",
            options,
            0,
            Messages.getQuestionIcon()
        )

        when (result) {
            0 -> {
                // 手动添加 - 打开搜索对话框
                StockerSuggestionDialog(project).show()
                // 对话框关闭后刷新列表
                refreshListModel(listModel)
            }
            1 -> {
                // 批量添加成交量TOP10
                addTopVolumeStocks(listModel)
            }
            else -> {
                // 取消
            }
        }
    }

    /**
     * 刷新列表模型，从设置中重新加载数据
     */
    private fun refreshListModel(listModel: DefaultListModel<StockerQuote>) {
        val quotes = when (currentMarketSelection) {
            StockerMarketType.AShare -> StockerQuoteHttpUtil.get(
                StockerMarketType.AShare,
                setting.quoteProvider,
                setting.aShareList
            )
            StockerMarketType.HKStocks -> StockerQuoteHttpUtil.get(
                StockerMarketType.HKStocks,
                setting.quoteProvider,
                setting.hkStocksList
            )
            StockerMarketType.USStocks -> StockerQuoteHttpUtil.get(
                StockerMarketType.USStocks,
                setting.quoteProvider,
                setting.usStocksList
            )
            else -> emptyList()
        }
        
        listModel.clear()
        listModel.addAll(quotes)
    }

    /**
     * 批量添加成交量前十的股票
     */
    private fun addTopVolumeStocks(listModel: DefaultListModel<StockerQuote>) {
        // 显示加载提示
        val progressTitle = when (currentMarketSelection) {
            StockerMarketType.AShare -> "正在获取A股成交量TOP10..."
            StockerMarketType.HKStocks -> "正在获取港股成交量TOP10..."
            StockerMarketType.USStocks -> "正在获取美股成交量TOP10..."
            else -> "正在获取数据..."
        }

        // 使用后台线程获取数据
        Thread {
            try {
                val topStocks = StockerHotStockUtil.getTopVolumeStocksWithDetails(
                    currentMarketSelection,
                    setting.quoteProvider,
                    10
                )

                // 在EDT线程中更新UI
                SwingUtilities.invokeLater {
                    if (topStocks.isEmpty()) {
                        Messages.showWarningDialog(
                            project,
                            "未能获取成交量排名数据，请稍后重试",
                            "获取失败"
                        )
                        return@invokeLater
                    }

                    var addedCount = 0
                    topStocks.forEach { stock ->
                        val exists = listModel.elements().asSequence().any { it.code == stock.code }
                        if (!exists) {
                            listModel.addElement(stock)
                            addedCount++
                        }
                    }

                    Messages.showInfoMessage(
                        project,
                        "成功添加 $addedCount 只股票（共${topStocks.size}只，已过滤重复）",
                        "添加成功"
                    )
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    Messages.showErrorDialog(
                        project,
                        "添加失败: ${e.message}",
                        "错误"
                    )
                }
            }
        }.start()
    }

}
