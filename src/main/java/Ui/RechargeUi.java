package Ui;

import entity.Member;
import service.ShopService;
import service.MemberService;
import service.ServiceResult;
import utils.LanguageUtils; // 引入
import utils.StyleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class RechargeUi extends JFrame {

    private MemberService memberService;
    private ShopService shopService;
    private Member currentMember;
    private JTextField searchField, amountField;
    private JLabel infoLabel, balanceLabel;
    private JButton confirmBtn;

    public RechargeUi() {
        this.memberService = new MemberService();
        this.shopService = new ShopService();
        StyleUtils.initGlobalTheme();
        setTitle("💰 " + LanguageUtils.getText("recharge.title"));
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(null);
        initView();
        setVisible(true);
    }

    private void initView() {
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new RechargeUi());
        langBtn.setBounds(500, 10, 70, 30);
        add(langBtn);

        JPanel searchPanel = new JPanel(null);
        searchPanel.setBounds(20, 40, 545, 100);
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        add(searchPanel);

        JLabel lbl1 = new JLabel("🔍 " + LanguageUtils.getText("recharge.step1"));
        lbl1.setFont(StyleUtils.FONT_BOLD);
        lbl1.setForeground(StyleUtils.COLOR_PRIMARY);
        lbl1.setBounds(20, 15, 300, 20);
        searchPanel.add(lbl1);

        searchField = new JTextField();
        searchField.setBounds(20, 45, 380, 40);
        StyleUtils.styleTextField(searchField);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) searchMember(); }
        });
        searchPanel.add(searchField);

        JButton searchBtn = new JButton(LanguageUtils.getText("btn.search"));
        searchBtn.setBounds(410, 45, 115, 40);
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> searchMember());
        searchPanel.add(searchBtn);

        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(20, 150, 545, 100);
        infoPanel.setBackground(new Color(240, 248, 255));
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(176, 224, 230), 1));
        add(infoPanel);

        infoLabel = new JLabel(LanguageUtils.getText("mm.col.name") + ": -   |   " + LanguageUtils.getText("mm.col.phone") + ": -");
        infoLabel.setFont(StyleUtils.FONT_NORMAL);
        infoLabel.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        infoLabel.setBounds(20, 25, 500, 25);
        infoPanel.add(infoLabel);

        balanceLabel = new JLabel(LanguageUtils.getText("recharge.balance") + ": ¥ 0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 22));
        balanceLabel.setForeground(StyleUtils.COLOR_SUCCESS);
        balanceLabel.setBounds(20, 55, 500, 30);
        infoPanel.add(balanceLabel);

        JPanel rechargePanel = new JPanel(null);
        rechargePanel.setBounds(20, 270, 545, 180);
        rechargePanel.setBackground(Color.WHITE);
        rechargePanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        add(rechargePanel);

        JLabel lbl2 = new JLabel("💰 " + LanguageUtils.getText("recharge.step2"));
        lbl2.setFont(StyleUtils.FONT_BOLD);
        lbl2.setForeground(StyleUtils.COLOR_WARNING);
        lbl2.setBounds(20, 15, 200, 20);
        rechargePanel.add(lbl2);

        amountField = new JTextField();
        amountField.setBounds(20, 45, 380, 45);
        amountField.setFont(new Font("Arial", Font.BOLD, 20));
        StyleUtils.styleTextField(amountField);
        rechargePanel.add(amountField);

        confirmBtn = new JButton(LanguageUtils.getText("recharge.btn"));
        confirmBtn.setBounds(410, 45, 115, 45);
        StyleUtils.styleButton(confirmBtn, StyleUtils.COLOR_SUCCESS);
        confirmBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        confirmBtn.setEnabled(false);
        confirmBtn.addActionListener(e -> performRecharge());
        rechargePanel.add(confirmBtn);

        addQuickBtn(rechargePanel, "¥ 100", 20, 110);
        addQuickBtn(rechargePanel, "¥ 500", 110, 110);
        addQuickBtn(rechargePanel, "¥ 1000", 200, 110);
    }

    private void addQuickBtn(JPanel panel, String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 80, 35);
        btn.setFont(StyleUtils.FONT_NORMAL);
        btn.setBackground(new Color(245, 245, 245));
        btn.addActionListener(e -> amountField.setText(text.replace("¥ ", "")));
        panel.add(btn);
    }

    private void searchMember() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { JOptionPane.showMessageDialog(this, "Empty keyword!"); return; }
        List<Member> list = memberService.search(keyword);
        if (list.isEmpty()) { JOptionPane.showMessageDialog(this, "Not Found!"); resetInfo(); }
        else {
            currentMember = list.get(0);
            infoLabel.setText(LanguageUtils.getText("mm.col.name") + ": " + currentMember.getName() + " | " + LanguageUtils.getText("mm.col.phone") + ": " + currentMember.getPhone());
            balanceLabel.setText(LanguageUtils.getText("recharge.balance") + ": ¥ " + String.format("%,.2f", currentMember.getBalance()));
            confirmBtn.setEnabled(true);
            amountField.requestFocus();
        }
    }

    private void resetInfo() {
        currentMember = null;
        infoLabel.setText("-"); balanceLabel.setText("¥ 0.00"); confirmBtn.setEnabled(false); amountField.setText("");
    }

    private void performRecharge() {
        if (currentMember == null) return;
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) return;
            if (JOptionPane.showConfirmDialog(this, LanguageUtils.getText("btn.confirm") + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                ServiceResult<Void> result = shopService.recharge(currentMember.getId(), amount);
                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "✅ " + result.getMessage());
                    searchMember();
                    amountField.setText("");
                } else JOptionPane.showMessageDialog(this, "❌ " + result.getMessage());
            }
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Invalid Number"); }
    }
}