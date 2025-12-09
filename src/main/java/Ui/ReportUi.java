package Ui;

import dao.StatisticsDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ReportUi extends JFrame {

    private StatisticsDAO statsDAO;
    private JTable orderTable;
    private DefaultTableModel tableModel;

    // 4个数据标签，方便刷新
    private JLabel revenueLabel;
    private JLabel memberCountLabel;
    private JLabel orderCountLabel;
    private JLabel stockAlertLabel;

    public ReportUi() {
        this.statsDAO = new StatisticsDAO();

        setTitle("经营数据仪表盘 (Dashboard)");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(15, 15)); // 整体布局

        initTopCards();   // 初始化顶部卡片
        initCenterTable(); // 初始化中间表格

        loadData(); // 加载数据

        setVisible(true);
    }

    /**
     * 初始化顶部 4 个统计卡片
     */
    private void initTopCards() {
        JPanel topPanel = new JPanel(new GridLayout(1, 4, 15, 0)); // 1行4列，间距15
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15)); // 外边距

        // 1. 总收入卡片
        revenueLabel = new JLabel("Loading...");
        topPanel.add(createCard("总营收 (Total)", revenueLabel, new Color(100, 149, 237))); // 矢车菊蓝

        // 2. 会员总数
        memberCountLabel = new JLabel("Loading...");
        topPanel.add(createCard("会员总数", memberCountLabel, new Color(60, 179, 113))); // 森林绿

        // 3. 今日订单
        orderCountLabel = new JLabel("Loading...");
        topPanel.add(createCard("今日订单数", orderCountLabel, new Color(255, 165, 0))); // 橙色

        // 4. 库存预警
        stockAlertLabel = new JLabel("Loading...");
        topPanel.add(createCard("库存紧张商品", stockAlertLabel, new Color(220, 20, 60))); // 猩红

        this.add(topPanel, BorderLayout.NORTH);
    }

    /**
     * 辅助方法：创建一个漂亮的卡片 Panel
     */
    private JPanel createCard(String title, JLabel valueLabel, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // 内边距

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLbl.setForeground(new Color(255, 255, 255, 200)); // 半透明白色

        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    /**
     * 初始化中间的交易流水表格
     */
    private void initCenterTable() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("最近交易流水 (Latest Transactions)"));

        // 刷新按钮
        JButton refreshBtn = new JButton("刷新数据");
        refreshBtn.addActionListener(e -> loadData());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(refreshBtn);
        centerPanel.add(btnPanel, BorderLayout.NORTH);

        // 表格
        String[] columns = {"订单ID", "客户", "消费类型", "金额 (¥)", "交易时间"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        orderTable = new JTable(tableModel);
        orderTable.setRowHeight(30);
        orderTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(orderTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        this.add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * 加载所有数据
     */
    private void loadData() {
        // 1. 加载顶部统计
        double revenue = statsDAO.getTotalRevenue();
        int members = statsDAO.getTotalMembers();
        int todayOrders = statsDAO.getTodayOrderCount();
        int lowStock = statsDAO.getLowStockProductCount();

        revenueLabel.setText("¥ " + String.format("%,.2f", revenue));
        memberCountLabel.setText(String.valueOf(members) + " 人");
        orderCountLabel.setText(String.valueOf(todayOrders) + " 单");
        stockAlertLabel.setText(String.valueOf(lowStock) + " 种");

        // 2. 加载表格
        tableModel.setRowCount(0);
        List<Map<String, Object>> orders = statsDAO.getRecentOrders();
        for (Map<String, Object> o : orders) {
            tableModel.addRow(new Object[]{
                    o.get("id"),
                    o.get("name"),
                    translateType((String)o.get("type")), // 翻译一下英文类型
                    String.format("%.2f", (double)o.get("amount")),
                    o.get("time")
            });
        }
    }

    // 简单翻译一下数据库里的英文类型
    private String translateType(String type) {
        if (type == null) return "-";
        switch (type) {
            case "product": return "商品消费";
            case "recharge": return "余额充值";
            case "membership": return "办卡/续费";
            case "course": return "课程费";
            default: return type;
        }
    }
}