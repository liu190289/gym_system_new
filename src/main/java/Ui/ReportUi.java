package Ui;

import dao.StatisticsDAO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import utils.LanguageUtils;
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ReportUi extends JFrame {

    private StatisticsDAO statsDAO;
    private JLabel revenueLabel, memberLabel, orderLabel, stockLabel;

    private DefaultCategoryDataset barDataset;
    private DefaultPieDataset pieDataset;
    private JFreeChart barChart;
    private JFreeChart pieChart;

    private JTable table;
    private DefaultTableModel tableModel;
    private CardLayout cardLayout;
    private JPanel centerPanel;

    public ReportUi() {
        this.statsDAO = new StatisticsDAO();
        StyleUtils.initGlobalTheme();
        setTitle("📊 " + LanguageUtils.getText("report.title"));
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(10, 10));

        initTopCards();
        initCenterViews();
        initBottomToolbar();

        refreshData();
        setVisible(true);
    }

    private void initTopCards() {
        JPanel topPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        revenueLabel = new JLabel("Loading...");
        topPanel.add(createCard(LanguageUtils.getText("report.revenue"), revenueLabel, new Color(108, 92, 231)));

        memberLabel = new JLabel("Loading...");
        topPanel.add(createCard(LanguageUtils.getText("report.members"), memberLabel, new Color(9, 132, 227)));

        orderLabel = new JLabel("Loading...");
        topPanel.add(createCard(LanguageUtils.getText("report.orders"), orderLabel, new Color(0, 184, 148)));

        stockLabel = new JLabel("Loading...");
        topPanel.add(createCard(LanguageUtils.getText("report.stock"), stockLabel, new Color(214, 48, 49)));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        langPanel.setOpaque(false);
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new ReportUi());
        langPanel.add(langBtn);

        topContainer.add(langPanel, BorderLayout.NORTH);
        topContainer.add(topPanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);
    }

    private JPanel createCard(String title, JLabel valueLabel, Color barColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(5, 0));
        bar.setBackground(barColor);
        card.add(bar, BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(StyleUtils.FONT_NORMAL);
        tLbl.setForeground(StyleUtils.COLOR_INFO);
        valueLabel.setFont(new Font(StyleUtils.FONT_NAME, Font.BOLD, 24)); // 修复字体
        valueLabel.setForeground(StyleUtils.COLOR_TEXT_MAIN);

        content.add(tLbl, BorderLayout.NORTH);
        content.add(valueLabel, BorderLayout.CENTER);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void initCenterViews() {
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        centerPanel.setOpaque(false);

        // 表格
        String[] cols = {"ID", "Name", "Type", "Amount", "Time", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        StyleUtils.styleTable(table);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        centerPanel.add(tableScroll, "TABLE");

        // 柱状图
        barDataset = new DefaultCategoryDataset();
        barChart = ChartFactory.createBarChart(
                LanguageUtils.getText("chart.bar.title"),
                LanguageUtils.getText("chart.bar.x"),
                LanguageUtils.getText("chart.bar.y"),
                barDataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        styleBarChart(barChart);
        ChartPanel barPanel = new ChartPanel(barChart);
        centerPanel.add(barPanel, "BAR");

        // 饼状图
        pieDataset = new DefaultPieDataset();
        pieChart = ChartFactory.createPieChart(
                LanguageUtils.getText("chart.pie.title"),
                pieDataset,
                true, true, false
        );
        stylePieChart(pieChart);
        ChartPanel piePanel = new ChartPanel(pieChart);
        centerPanel.add(piePanel, "PIE");

        add(centerPanel, BorderLayout.CENTER);
    }

    private void initBottomToolbar() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        bottomPanel.add(createSwitchBtn("📄 " + LanguageUtils.getText("report.tab.table"), "TABLE", StyleUtils.COLOR_PRIMARY));
        bottomPanel.add(createSwitchBtn("📊 " + LanguageUtils.getText("report.tab.bar"), "BAR", StyleUtils.COLOR_SUCCESS));
        bottomPanel.add(createSwitchBtn("🍰 " + LanguageUtils.getText("report.tab.pie"), "PIE", StyleUtils.COLOR_WARNING));

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createSwitchBtn(String text, String cardName, Color color) {
        JButton btn = new JButton(text);
        StyleUtils.styleButton(btn, color);
        btn.setPreferredSize(new Dimension(140, 40));
        btn.addActionListener(e -> cardLayout.show(centerPanel, cardName));
        return btn;
    }

    /**
     * 核心修改逻辑：这里进行数据的翻译映射
     */
    private void refreshData() {
        // 1. 卡片
        revenueLabel.setText(String.format("¥ %,.2f", statsDAO.getTotalRevenue()));
        memberLabel.setText(String.valueOf(statsDAO.getTotalMembers()));
        orderLabel.setText(String.valueOf(statsDAO.getTodayOrderCount()));
        stockLabel.setText(String.valueOf(statsDAO.getLowStockProductCount()));

        // 2. 表格
        tableModel.setRowCount(0);
        List<Map<String, Object>> orders = statsDAO.getRecentOrders();
        for (Map<String, Object> o : orders) {
            tableModel.addRow(new Object[]{
                    o.get("id"), o.get("name"), o.get("type"),
                    String.format("¥ %.2f", o.get("amount")),
                    o.get("time"), o.get("status")
            });
        }

        // 3. 柱状图 (演示数据)
        barDataset.clear();
        String seriesName = LanguageUtils.getText("chart.series.revenue");
        barDataset.addValue(5000, seriesName, "Mon");
        barDataset.addValue(7000, seriesName, "Tue");
        barDataset.addValue(6000, seriesName, "Wed");
        barDataset.addValue(8000, seriesName, "Thu");
        barDataset.addValue(4000, seriesName, "Fri");

        // 4. 饼状图 (关键修改点！)
        pieDataset.clear();
        Map<String, Double> pieData = statsDAO.getRevenueByType();

        for (Map.Entry<String, Double> entry : pieData.entrySet()) {
            String rawKey = entry.getKey(); // 数据库原始值 (例如 "商品售卖")
            String displayKey = rawKey;     // 默认显示原始值

            // === 翻译映射逻辑 ===
            // 检查数据库里可能的中文 Key，并映射到 LanguageUtils
            if (rawKey.contains("商品") || rawKey.equalsIgnoreCase("Product")) {
                displayKey = LanguageUtils.getText("cat.product");
            } else if (rawKey.contains("续费") || rawKey.equalsIgnoreCase("Renew")) {
                displayKey = LanguageUtils.getText("cat.renew");
            } else if (rawKey.contains("会员") || rawKey.equalsIgnoreCase("Membership")) {
                displayKey = LanguageUtils.getText("cat.membership");
            } else if (rawKey.contains("充值") || rawKey.equalsIgnoreCase("Recharge")) {
                displayKey = LanguageUtils.getText("cat.recharge");
            } else {
                displayKey = LanguageUtils.getText("cat.other") + " (" + rawKey + ")";
            }

            pieDataset.setValue(displayKey, entry.getValue());
        }
    }

    private void styleBarChart(JFreeChart chart) {
        try {
            chart.getTitle().setFont(StyleUtils.FONT_TITLE);
            CategoryPlot plot = chart.getCategoryPlot();

            // 确保坐标轴也使用全局字体，支持中文
            Font axisFont = new Font(StyleUtils.FONT_NAME, Font.BOLD, 12);
            Font labelFont = new Font(StyleUtils.FONT_NAME, Font.PLAIN, 12);

            plot.getDomainAxis().setLabelFont(axisFont);
            plot.getDomainAxis().setTickLabelFont(labelFont);
            plot.getRangeAxis().setLabelFont(axisFont);
            plot.getRangeAxis().setTickLabelFont(labelFont);

            LegendTitle legend = chart.getLegend();
            if (legend != null) legend.setItemFont(labelFont);

            chart.setBackgroundPaint(Color.WHITE);
            plot.setBackgroundPaint(new Color(250, 250, 250));
            plot.setRangeGridlinePaint(new Color(220, 220, 220));
            plot.setOutlineVisible(false);

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setBarPainter(new StandardBarPainter());
            renderer.setSeriesPaint(0, new Color(108, 92, 231));
            renderer.setDrawBarOutline(false);
            renderer.setShadowVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stylePieChart(JFreeChart chart) {
        try {
            chart.getTitle().setFont(StyleUtils.FONT_TITLE);
            PiePlot plot = (PiePlot) chart.getPlot();

            // 饼图标签字体
            plot.setLabelFont(new Font(StyleUtils.FONT_NAME, Font.PLAIN, 12));

            LegendTitle legend = chart.getLegend();
            if (legend != null) legend.setItemFont(new Font(StyleUtils.FONT_NAME, Font.PLAIN, 12));

            chart.setBackgroundPaint(Color.WHITE);
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            plot.setShadowPaint(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}