package com.vermouthx.stocker.views;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.vermouthx.stocker.components.StockerDefaultTableCellRender;
import com.vermouthx.stocker.components.StockerTableHeaderRender;
import com.vermouthx.stocker.components.StockerTableModel;
import com.vermouthx.stocker.components.StockerTableSorter;
import com.vermouthx.stocker.entities.StockerQuote;
import com.vermouthx.stocker.settings.StockerSetting;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StockerTableView {

    private JPanel mPane;
    private JScrollPane tbPane;
    private Color upColor;
    private Color downColor;
    private Color zeroColor;
    private JBTable tbBody;
    private StockerTableModel tbModel;
    private StockerTableSorter sorter;

    private final ComboBox<String> cbIndex = new ComboBox<>();
    private final JBLabel lbIndexValue = new JBLabel("", SwingConstants.CENTER);
    private final JBLabel lbIndexExtent = new JBLabel("", SwingConstants.CENTER);
    private final JBLabel lbIndexPercent = new JBLabel("", SwingConstants.CENTER);
    private List<StockerQuote> indices = new ArrayList<>();

    public StockerTableView() {
        syncColorPatternSetting();
        initPane();
        initTable();
    }

    public void syncIndices(List<StockerQuote> indices) {
        this.indices = indices;
        if (cbIndex.getItemCount() == 0 && !indices.isEmpty()) {
            indices.forEach(i -> cbIndex.addItem(i.getName()));
            cbIndex.setSelectedIndex(0);
        }
        syncColorPatternSetting();
        updateIndex();
    }

    private void syncColorPatternSetting() {
        StockerSetting setting = StockerSetting.Companion.getInstance();
        switch (setting.getQuoteColorPattern()) {
            case RED_UP_GREEN_DOWN:
                upColor = JBColor.RED;
                downColor = JBColor.GREEN;
                zeroColor = JBColor.GRAY;
                break;
            case GREEN_UP_RED_DOWN:
                upColor = JBColor.GREEN;
                downColor = JBColor.RED;
                zeroColor = JBColor.GRAY;
                break;
            default:
                upColor = JBColor.foreground();
                downColor = JBColor.foreground();
                zeroColor = JBColor.foreground();
                break;
        }
    }

    private void updateIndex() {
        if (cbIndex.getSelectedIndex() != -1) {
            String name = Objects.requireNonNull(cbIndex.getSelectedItem()).toString();
            for (StockerQuote index : indices) {
                if (index.getName().equals(name)) {
                    lbIndexValue.setText(Double.toString(index.getCurrent()));
                    lbIndexExtent.setText(Double.toString(index.getChange()));
                    lbIndexPercent.setText(index.getPercentage() + "%");
                    double value = index.getPercentage();
                    if (value > 0) {
                        lbIndexValue.setForeground(upColor);
                        lbIndexExtent.setForeground(upColor);
                        lbIndexPercent.setForeground(upColor);
                    } else if (value < 0) {
                        lbIndexValue.setForeground(downColor);
                        lbIndexExtent.setForeground(downColor);
                        lbIndexPercent.setForeground(downColor);
                    } else {
                        lbIndexValue.setForeground(zeroColor);
                        lbIndexExtent.setForeground(zeroColor);
                        lbIndexPercent.setForeground(zeroColor);
                    }
                    break;
                }
            }
        }
    }

    private void initPane() {
        tbPane = new JBScrollPane();
        tbPane.setBorder(BorderFactory.createEmptyBorder());
        JPanel iPane = new JPanel(new GridLayout(1, 4));
        iPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()));
        iPane.add(cbIndex);
        iPane.add(lbIndexValue);
        iPane.add(lbIndexExtent);
        iPane.add(lbIndexPercent);
        cbIndex.addItemListener(i -> updateIndex());
        mPane = new JPanel(new BorderLayout());
        mPane.add(tbPane, BorderLayout.CENTER);
        // summary panel placeholder, will be added below in initTable when model is ready
        mPane.add(iPane, BorderLayout.SOUTH);
    }

    private static final String codeColumn = "Symbol";
    private static final String nameColumn = "Name";
    private static final String currentColumn = "Current";
    private static final String percentColumn = "Change%";
    private static final String costColumn = "Cost Price";
    private static final String qtyColumn = "Quantity";
    private static final String dayPlColumn = "Day P&L";
    private static final String plColumn = "P&L";

    private void initTable() {
        tbModel = new StockerTableModel();
        tbBody = new JBTable();
        tbBody.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int row = tbBody.rowAtPoint(e.getPoint());
                if (row >= 0 && row < tbBody.getRowCount()) {
                    if (tbBody.getSelectedRows().length == 0 || Arrays.stream(tbBody.getSelectedRows()).noneMatch(p -> p == row)) {
                        tbBody.setRowSelectionInterval(row, row);
                    }
                } else {
                    tbBody.clearSelection();
                }
            }
        });
        tbModel.setColumnIdentifiers(new String[]{codeColumn, nameColumn, currentColumn, percentColumn, costColumn, qtyColumn, dayPlColumn, plColumn});

        tbBody.setShowVerticalLines(false);
        tbBody.setModel(tbModel);

        tbBody.getTableHeader().setReorderingAllowed(false);
        
        // 使用自定义排序器
        sorter = new StockerTableSorter(tbModel);
        tbBody.setRowSorter(sorter);
        
        // 添加表头点击监听器，实现升序/降序切换
        tbBody.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = tbBody.getTableHeader().columnAtPoint(e.getPoint());
                if (column >= 0) {
                    toggleSortOrder(sorter, column);
                }
            }
        });
        
        tbBody.getTableHeader().setDefaultRenderer(new StockerTableHeaderRender(tbBody));

        // hide Symbol column visually but keep data in model
        try {
            javax.swing.table.TableColumn c = tbBody.getColumn(codeColumn);
            c.setMinWidth(0);
            c.setMaxWidth(0);
            c.setPreferredWidth(0);
            c.setResizable(false);
        } catch (IllegalArgumentException ignore) {
        }

        tbBody.getColumn(codeColumn).setCellRenderer(new StockerDefaultTableCellRender() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row == table.getRowCount() - 1) {
                    c.setVisible(false);
                }
                return c;
            }
        });
        tbBody.getColumn(nameColumn).setCellRenderer(new StockerDefaultTableCellRender() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row == table.getRowCount() - 1) {
                    c.setVisible(false);
                }
                return c;
            }
        });
        tbBody.getColumn(currentColumn).setCellRenderer(new StockerDefaultTableCellRender() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row == table.getRowCount() - 1) {
                    c.setVisible(false);
                    return c;
                }
                syncColorPatternSetting();
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                try {
                    String percent = table.getValueAt(row, table.getColumn(percentColumn).getModelIndex()).toString();
                    int percentIndex = percent.indexOf("%");
                    if (percentIndex > 0) {
                        Double v = Double.parseDouble(percent.substring(0, percentIndex));
                        applyColorPatternToTable(v, this);
                    }
                } catch (Exception ignore) {
                    // 解析失败时使用默认颜色
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        tbBody.getColumn(percentColumn).setCellRenderer(new StockerDefaultTableCellRender() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row == table.getRowCount() - 1) {
                    c.setVisible(false);
                    return c;
                }
                syncColorPatternSetting();
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                try {
                    if (value != null) {
                        String percent = value.toString();
                        int percentIndex = percent.indexOf("%");
                        if (percentIndex > 0) {
                            Double v = Double.parseDouble(percent.substring(0, percentIndex));
                            applyColorPatternToTable(v, this);
                        }
                    }
                } catch (Exception ignore) {
                    // 解析失败时使用默认颜色
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        // align cost column center
        tbBody.getColumn(costColumn).setCellRenderer(new StockerDefaultTableCellRender() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row == table.getRowCount() - 1) {
                    c.setVisible(false);
                    return c;
                }
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        // align quantity column center
        tbBody.getColumn(qtyColumn).setCellRenderer(new StockerDefaultTableCellRender() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row == table.getRowCount() - 1) {
                    c.setVisible(false);
                    return c;
                }
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        // align Day P/L column center
        tbBody.getColumn(dayPlColumn).setCellRenderer(new StockerDefaultTableCellRender() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                // special handling for summary row (first row)
                if (row == 0) {
                    setText("Total Day P&L: " + (value != null ? value.toString() : "0.00"));
                    setFont(getFont().deriveFont(Font.BOLD));
                    // 汇总行也应用颜色
                    try {
                        syncColorPatternSetting();
                        double v = 0.0;
                        if (value != null) {
                            String s = value.toString().trim();
                            if (!s.isEmpty()) {
                                v = Double.parseDouble(s);
                            }
                        }
                        applyColorPatternToTable(v, this);
                    } catch (Exception ignore) {
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
                // color by sign using current color pattern
                try {
                    syncColorPatternSetting();
                    double v = 0.0;
                    if (value != null) {
                        String s = value.toString().trim();
                        if (!s.isEmpty()) {
                            v = Double.parseDouble(s);
                        }
                    }
                    applyColorPatternToTable(v, this);
                } catch (Exception ignore) {
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        // align P/L column center
        tbBody.getColumn(plColumn).setCellRenderer(new StockerDefaultTableCellRender() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                // special handling for summary row (first row)
                if (row == 0) {
                    setText("Total P&L: " + (value != null ? value.toString() : "0.00"));
                    setFont(getFont().deriveFont(Font.BOLD));
                    // 汇总行也应用颜色
                    try {
                        syncColorPatternSetting();
                        double v = 0.0;
                        if (value != null) {
                            String s = value.toString().trim();
                            if (!s.isEmpty()) {
                                v = Double.parseDouble(s);
                            }
                        }
                        applyColorPatternToTable(v, this);
                    } catch (Exception ignore) {
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
                // color by sign using current color pattern
                try {
                    syncColorPatternSetting();
                    double v = 0.0;
                    if (value != null) {
                        String s = value.toString().trim();
                        if (!s.isEmpty()) {
                            v = Double.parseDouble(s);
                        }
                    }
                    applyColorPatternToTable(v, this);
                } catch (Exception ignore) {
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        // persist edits to settings on cost column changes
        tbModel.addTableModelListener(e -> {
            int column = e.getColumn();
            if (column == tbBody.getColumn(costColumn).getModelIndex() || column == tbBody.getColumn(qtyColumn).getModelIndex()) {
                int first = e.getFirstRow();
                int last = e.getLastRow();
                if (first >= 0) {
                    StockerSetting setting = StockerSetting.Companion.getInstance();
                    for (int r = first; r <= last; r++) {
                        // skip summary row (last row)
                        if (r == tbModel.getRowCount() - 1) continue;
                        Object codeObj = tbModel.getValueAt(r, tbBody.getColumn(codeColumn).getModelIndex());
                        Object costObj = tbModel.getValueAt(r, tbBody.getColumn(costColumn).getModelIndex());
                        Object qtyObj = tbModel.getValueAt(r, tbBody.getColumn(qtyColumn).getModelIndex());
                        if (codeObj != null) {
                            String code = codeObj.toString();
                            String cost = costObj == null ? null : costObj.toString();
                            String qty = qtyObj == null ? null : qtyObj.toString();
                            setting.setCost(code, cost);
                            setting.setQuantity(code, qty);
                            // recalc P/L and Day P/L
                            try {
                                double current = Double.parseDouble(tbModel.getValueAt(r, tbBody.getColumn(currentColumn).getModelIndex()).toString());
                                String percentStr = tbModel.getValueAt(r, tbBody.getColumn(percentColumn).getModelIndex()).toString();
                                int percentIndex = percentStr.indexOf("%");
                                if (percentIndex <= 0) {
                                    throw new IllegalArgumentException("Invalid percent format");
                                }
                                double percent = Double.parseDouble(percentStr.substring(0, percentIndex));
                                double c = cost == null || cost.isBlank() ? 0.0 : Double.parseDouble(cost);
                                double q = qty == null || qty.isBlank() ? 0.0 : Double.parseDouble(qty);
                                double pl = (current - c) * q;
                                tbModel.setValueAt(String.format("%.2f", pl), r, tbBody.getColumn(plColumn).getModelIndex());
                                tbModel.fireTableCellUpdated(r, tbBody.getColumn(plColumn).getModelIndex());
                                // calculate change from current price and percentage
                                double change = current * percent / 100.0;
                                double dayPl = change * q;
                                tbModel.setValueAt(String.format("%.2f", dayPl), r, tbBody.getColumn(dayPlColumn).getModelIndex());
                                tbModel.fireTableCellUpdated(r, tbBody.getColumn(dayPlColumn).getModelIndex());
                            } catch (Exception ignore) {
                                tbModel.setValueAt("", r, tbBody.getColumn(plColumn).getModelIndex());
                                tbModel.setValueAt("", r, tbBody.getColumn(dayPlColumn).getModelIndex());
                            }
                        }
                    }
                    // update summary row
                    updateSummaryRow();
                }
            }
        });
        tbPane.add(tbBody);
        tbPane.setViewportView(tbBody);
        
        // initial summary update
        updateSummaryRow();
    }
    
    private void updateSummaryRow() {
        try {
            // 先删除所有现有的汇总行（可能因为排序等原因有多个）
            for (int r = tbModel.getRowCount() - 1; r >= 0; r--) {
                Object codeObj = tbModel.getValueAt(r, 0);
                if (codeObj != null && "SUMMARY".equals(codeObj.toString())) {
                    tbModel.removeRow(r);
                }
            }
            
            int idxCurrent = tbBody.getColumn(currentColumn).getModelIndex();
            int idxCost = tbBody.getColumn(costColumn).getModelIndex();
            int idxQty = tbBody.getColumn(qtyColumn).getModelIndex();
            int idxDayPl = tbBody.getColumn(dayPlColumn).getModelIndex();
            int idxPl = tbBody.getColumn(plColumn).getModelIndex();
            
            double totalPl = 0.0;
            double totalDayPl = 0.0;
            double totalMarketValue = 0.0;  // 总持仓市值
            
            // 计算所有行的汇总（现在不包含汇总行了）
            for (int r = 0; r < tbModel.getRowCount(); r++) {
                Object qtyObj = tbModel.getValueAt(r, idxQty);
                String qtyStr = qtyObj == null ? "" : qtyObj.toString().trim();
                double q = 0.0;
                try { q = qtyStr.isEmpty() ? 0.0 : Double.parseDouble(qtyStr); } catch (Exception ignore) {}
                if (q == 0.0) continue;
                
                double current;
                try { current = Double.parseDouble(String.valueOf(tbModel.getValueAt(r, idxCurrent))); } catch (Exception ex) { current = 0.0; }
                double cost;
                try { cost = Double.parseDouble(String.valueOf(tbModel.getValueAt(r, idxCost))); } catch (Exception ex) { cost = 0.0; }
                
                totalPl += (current - cost) * q;
                totalMarketValue += current * q;  // 累计市值 = 当前价格 × 持仓数量
                
                // calculate day P/L from Day P&L column
                Object dayPlObj = tbModel.getValueAt(r, idxDayPl);
                if (dayPlObj != null) {
                    try {
                        totalDayPl += Double.parseDouble(dayPlObj.toString().trim());
                    } catch (Exception ignore) {}
                }
            }
            
            // 添加新的汇总行到第一行
            // 总市值显示在持仓数量列
            tbModel.insertRow(0, new Object[]{
                "SUMMARY", 
                "Total", 
                "", 
                "", 
                "", 
                String.format("%.2f", totalMarketValue),  // 总市值放在 Quantity 列
                String.format("%.2f", totalDayPl), 
                String.format("%.2f", totalPl)
            });
        } catch (Exception ignore) {
            // 如果出错，确保至少有一个汇总行
            boolean hasSummary = false;
            for (int r = 0; r < tbModel.getRowCount(); r++) {
                Object codeObj = tbModel.getValueAt(r, 0);
                if (codeObj != null && "SUMMARY".equals(codeObj.toString())) {
                    hasSummary = true;
                    break;
                }
            }
            if (!hasSummary) {
                tbModel.insertRow(0, new Object[]{"SUMMARY", "Total", "", "", "", "0.00", "0.00", "0.00"});
            }
        }
    }

    private void applyColorPatternToTable(Double value, DefaultTableCellRenderer renderer) {
        if (value > 0) {
            renderer.setForeground(upColor);
        } else if (value < 0) {
            renderer.setForeground(downColor);
        } else {
            renderer.setForeground(zeroColor);
        }
    }

    /**
     * 切换列的排序顺序：无排序 -> 降序 -> 升序 -> 降序 -> ...
     */
    private void toggleSortOrder(StockerTableSorter sorter, int column) {
        List<RowSorter.SortKey> sortKeys = new ArrayList<>(sorter.getSortKeys());
        
        SortOrder newOrder;
        
        if (sortKeys.isEmpty() || sortKeys.get(0).getColumn() != column) {
            // 如果当前没有排序或者点击的是不同的列，设置为降序
            newOrder = SortOrder.DESCENDING;
        } else {
            // 如果点击的是当前排序列，切换排序顺序
            SortOrder currentOrder = sortKeys.get(0).getSortOrder();
            if (currentOrder == SortOrder.DESCENDING) {
                newOrder = SortOrder.ASCENDING;
            } else {
                newOrder = SortOrder.DESCENDING;
            }
        }
        
        // 设置新的排序
        sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(column, newOrder)));
        
        // 排序后确保汇总行在最后 - 使用 updateSummaryRow 重新生成，更可靠
        SwingUtilities.invokeLater(() -> {
            updateSummaryRow();
            tbBody.repaint(); // 刷新表格显示
        });
        
        // 刷新表头以显示排序指示器
        tbBody.getTableHeader().repaint();
    }

    public JComponent getComponent() {
        return mPane;
    }

    public JBTable getTableBody() {
        return tbBody;
    }

    public DefaultTableModel getTableModel() {
        return tbModel;
    }

    /**
     * 公共方法，供外部调用更新汇总行
     */
    public void updateSummaryRowPublic() {
        updateSummaryRow();
    }

}
