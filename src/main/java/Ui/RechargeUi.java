package Ui;

import entity.Member;
import service.ShopService;
import service.MemberService;
import service.ServiceResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RechargeUi extends JFrame {

    private MemberService memberService;
    private ShopService shopService;
    private Member currentMember;

    // 组件
    private JTextField searchField;
    private JLabel infoLabel;
    private JLabel balanceLabel;
    private JTextField amountField;
    private JButton confirmBtn;

    public RechargeUi() {
        this.memberService = new MemberService();
        this.shopService = new ShopService();
        setTitle("会员余额充值");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);

        initView();
        setVisible(true);
    }

    private void initView() {
        int x = 50, w = 380, h = 30;

        // --- 1. 搜索区域 ---
        JLabel lbl1 = new JLabel("第一步：搜索会员 (输入手机号或姓名)");
        lbl1.setFont(new Font("微软雅黑", Font.BOLD, 14));
        lbl1.setBounds(x, 20, w, h);
        add(lbl1);

        searchField = new JTextField();
        searchField.setBounds(x, 55, 260, 35);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) searchMember();
            }
        });
        add(searchField);

        JButton searchBtn = new JButton("查找");
        searchBtn.setBounds(320, 55, 110, 35);
        searchBtn.addActionListener(e -> searchMember());
        add(searchBtn);

        // --- 2. 信息展示区域 ---
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(x, 110, w, 100);
        infoPanel.setBorder(BorderFactory.createTitledBorder("会员信息"));
        add(infoPanel);

        infoLabel = new JLabel("姓名：-   |   手机：-");
        infoLabel.setBounds(20, 25, 300, 25);
        infoPanel.add(infoLabel);

        balanceLabel = new JLabel("当前余额：¥0.00");
        balanceLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        balanceLabel.setForeground(new Color(0, 100, 0)); // 深绿色
        balanceLabel.setBounds(20, 55, 300, 25);
        infoPanel.add(balanceLabel);

        // --- 3. 充值区域 ---
        JLabel lbl2 = new JLabel("第二步：输入充值金额");
        lbl2.setFont(new Font("微软雅黑", Font.BOLD, 14));
        lbl2.setBounds(x, 230, w, h);
        add(lbl2);

        amountField = new JTextField();
        amountField.setBounds(x, 260, 260, 35);
        amountField.setFont(new Font("Arial", Font.BOLD, 16));
        add(amountField);

        confirmBtn = new JButton("确认充值");
        confirmBtn.setBounds(320, 260, 110, 35);
        confirmBtn.setBackground(new Color(255, 140, 0)); // 橙色
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setEnabled(false); // 没选人之前不能按
        confirmBtn.addActionListener(e -> performRecharge());
        add(confirmBtn);
    }

    private void searchMember() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) return;

        java.util.List<Member> list = memberService.search(keyword);
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "未找到会员！");
            resetInfo();
        } else {
            // 默认选第一个
            currentMember = list.get(0);
            infoLabel.setText("姓名：" + currentMember.getName() + "   |   手机：" + currentMember.getPhone());
            balanceLabel.setText("当前余额：¥" + String.format("%.2f", currentMember.getBalance()));
            confirmBtn.setEnabled(true);

            if (list.size() > 1) {
                JOptionPane.showMessageDialog(this, "找到多个结果，已自动加载第一个。建议使用手机号精确查找。");
            }
        }
    }

    private void resetInfo() {
        currentMember = null;
        infoLabel.setText("姓名：-   |   手机：-");
        balanceLabel.setText("当前余额：¥0.00");
        confirmBtn.setEnabled(false);
        amountField.setText("");
    }

    private void performRecharge() {
        if (currentMember == null) return;

        String amountStr = amountField.getText().trim();
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "充值金额必须大于0！");
                return;
            }
            if (amount > 100000) {
                JOptionPane.showMessageDialog(this, "单次充值金额过大，请确认输入是否正确！");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "会员：" + currentMember.getName() + "\n" +
                            "充值金额：¥" + amount + "\n\n" +
                            "确认立即充值吗？",
                    "充值确认", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // 调用 ShopService 的充值方法
                ServiceResult<Void> result = shopService.recharge(currentMember.getId(), amount);

                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "✅ " + result.getMessage());
                    // 充值成功后，重新搜索刷新余额显示
                    searchMember();
                    amountField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "❌ " + result.getMessage());
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的金额数字！");
        }
    }
}