package Ui;

import dao.StatisticsDAO;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import utils.LanguageUtils; // 导入
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ReportUi extends JFrame {

    private StatisticsDAO statsDAO;
    private JLabel revenueLabel, memberLabel, orderLabel, stockLabel;
    private JPanel centerPanel;
    private CardLayout cardLayout;
    private DefaultCategoryDataset barDataset;
    private DefaultPieDataset pieDataset;
    private JScrollPane tableScroll;

    public ReportUi() {
        this.statsDAO = new StatisticsDAO();
        StyleUtils.initGlobalTheme(); // 关键：加载字体

        setTitle("📊 " + LanguageUtils.getText("report.title"));
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(15, 15));

        initTopCards();
        initCenterViews();
        initBottomToolbar();
        refreshData();
        setVisible(true);
    }

    private void initTopCards() {
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);

        // 语言切换按钮
        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        langPanel.setOpaque(false);
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new ReportUi());
        langPanel.add(langBtn);
        topContainer.add(langPanel, BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        revenueLabel = new JLabel("Loading...");
        topPanel.add(createCard("💰 " + LanguageUtils.getText("report.revenue"), revenueLabel, new Color(108, 92, 231)));

        memberLabel = new JLabel("Loading...");
        topPanel.add(createCard("👥 " + LanguageUtils.getText("report.members"), memberLabel, new Color(0, 184, 148)));

        orderLabel = new JLabel("Loading...");
        topPanel.add(createCard("📝 " + LanguageUtils.getText("report.orders"), orderLabel, new Color(253, 203, 110)));

        stockLabel = new JLabel("Loading...");
        topPanel.add(createCard("📦 " + LanguageUtils.getText("report.stock"), stockLabel, new Color(214, 48, 49)));

        topContainer.add(topPanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);
    }

    private JPanel createCard(String title, JLabel valueLabel, Color barColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(5, 0));
        bar.setBackground(barColor);
        card.add(bar, BorderLayout.WEST);
        JPanel content = new JPanel(new GridLayout(2, 1));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        JLabel tLbl = new JLabel(title);
        tLbl.setFont(StyleUtils.FONT_NORMAL);
        tLbl.setForeground(StyleUtils.COLOR_INFO);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        content.add(tLbl);
        content.add(valueLabel);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void initCenterViews() {
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        centerPanel.setOpaque(false);

        JTable table = new JTable(new DefaultTableModel(new String[]{"ID", "Name", "Type", "Amount", "Time", "Status"}, 0));
        StyleUtils.styleTable(table);
        tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        centerPanel.add(tableScroll, "TABLE");

        barDataset = new DefaultCategoryDataset();
        JFreeChart barChart = ChartFactory.createBarChart(LanguageUtils.getText("report.tab.bar"), "Type", "Amount", barDataset, PlotOrientation.VERTICAL, false, true, false);
        styleBarChart(barChart);
        ChartPanel barPanel = new ChartPanel(barChart);
        barPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        centerPanel.add(barPanel, "BAR");

        pieDataset = new DefaultPieDataset();
        JFreeChart pieChart = ChartFactory.createPieChart(LanguageUtils.getText("report.tab.pie"), pieDataset, true, true, false);
        stylePieChart(pieChart);
        ChartPanel piePanel = new ChartPanel(pieChart);
        piePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        centerPanel.add(piePanel, "PIE");

        add(centerPanel, BorderLayout.CENTER);
    }

    private void initBottomToolbar() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        bottomPanel.add(createSwitchBtn("📋 " + LanguageUtils.getText("report.tab.table"), "TABLE", StyleUtils.COLOR_PRIMARY));
        bottomPanel.add(createSwitchBtn("📊 " + LanguageUtils.getText("report.tab.bar"), "BAR", new Color(255, 159, 67)));
        bottomPanel.add(createSwitchBtn("🍰 " + LanguageUtils.getText("report.tab.pie"), "PIE", new Color(72, 219, 251)));
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createSwitchBtn(String text, String cardName, Color color) {
        JButton btn = new JButton(text);
        StyleUtils.styleButton(btn, color);
        btn.setPreferredSize(new Dimension(160, 45));
        btn.addActionListener(e -> cardLayout.show(centerPanel, cardName));
        return btn;
    }

    private void refreshData() {
        revenueLabel.setText("¥ " + String.format("%,.2f", statsDAO.getTotalRevenue()));
        memberLabel.setText(String.valueOf(statsDAO.getTotalMembers()));
        orderLabel.setText(String.valueOf(statsDAO.getTodayOrderCount()));
        stockLabel.setText(String.valueOf(statsDAO.getLowStockProductCount()));

        List<Map<String, Object>> orders = statsDAO.getRecentOrders();
        JTable table = (JTable) tableScroll.getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (Map<String, Object> o : orders) {
            model.addRow(new Object[]{o.get("id"), o.get("name"), o.get("type"), String.format("¥ %.2f", o.get("amount")), o.get("time"), o.get("status")});
        }
        try {
            Map<String, Double> data = statsDAO.getRevenueByType();
            barDataset.clear(); pieDataset.clear();
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                barDataset.setValue(entry.getValue(), "Revenue", entry.getKey());
                pieDataset.setValue(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {}
    }

    private void styleBarChart(JFreeChart chart) {
        // 设置中文字体，防止乱码
        Font font = new Font("微软雅黑", Font.PLAIN, 12);
        chart.getTitle().setFont(new Font("微软雅黑", Font.BOLD, 18));
        chart.getCategoryPlot().getDomainAxis().setLabelFont(font);
        chart.getCategoryPlot().getDomainAxis().setTickLabelFont(font);
        chart.getCategoryPlot().getRangeAxis().setLabelFont(font);
        chart.getCategoryPlot().getRangeAxis().setTickLabelFont(font);

        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlineVisible(false);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new Color(108, 92, 231));
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);
    }

    private void stylePieChart(JFreeChart chart) {
        Font font = new Font("微软雅黑", Font.PLAIN, 12);
        chart.getTitle().setFont(new Font("微软雅黑", Font.BOLD, 18));
        chart.getLegend().setItemFont(font);

        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(font);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
    }
}