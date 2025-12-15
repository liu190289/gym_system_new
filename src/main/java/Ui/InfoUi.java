package Ui;

import entity.Member;
import utils.LanguageUtils; // 引入
import utils.StyleUtils;

import javax.swing.*;
import java.awt.*;

public class InfoUi extends JFrame {

    private Member member;

    public InfoUi(Member member) {
        this.member = member;
        StyleUtils.initGlobalTheme();
        setTitle(LanguageUtils.getText("info.title"));
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(null);
        initView();
    }

    private void initView() {
        // 语言切换
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new InfoUi(member));
        langBtn.setBounds(300, 5, 70, 25);
        add(langBtn);

        JPanel cardPanel = new JPanel(null);
        cardPanel.setBounds(20, 35, 345, 400); // 调整位置
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        add(cardPanel);

        JLabel avatarLbl = new JLabel("🤠", SwingConstants.CENTER);
        avatarLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        avatarLbl.setBounds(0, 20, 345, 80);
        cardPanel.add(avatarLbl);

        JLabel nameLbl = new JLabel(member.getName(), SwingConstants.CENTER);
        nameLbl.setFont(StyleUtils.FONT_TITLE);
        nameLbl.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        nameLbl.setBounds(0, 100, 345, 30);
        cardPanel.add(nameLbl);

        JLabel idLbl = new JLabel("ID: " + member.getId(), SwingConstants.CENTER);
        idLbl.setFont(StyleUtils.FONT_NORMAL);
        idLbl.setForeground(StyleUtils.COLOR_INFO);
        idLbl.setBounds(0, 130, 345, 20);
        cardPanel.add(idLbl);

        JSeparator sep = new JSeparator();
        sep.setBounds(40, 160, 265, 1);
        cardPanel.add(sep);

        int startY = 180;
        int gap = 35;

        addInfoRow(cardPanel, "📱 " + LanguageUtils.getText("info.phone") + ":", member.getPhone(), startY);
        addInfoRow(cardPanel, "📧 " + LanguageUtils.getText("info.email") + ":", member.getEmail(), startY + gap);
        String gender = "male".equals(member.getGender()) ? LanguageUtils.getText("mm.gender.male") : LanguageUtils.getText("mm.gender.female");
        addInfoRow(cardPanel, "🚻 " + LanguageUtils.getText("info.gender") + ":", gender, startY + gap * 2);

        JLabel balanceKey = new JLabel("💰 " + LanguageUtils.getText("info.balance") + ":");
        balanceKey.setFont(StyleUtils.FONT_BOLD);
        balanceKey.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        balanceKey.setBounds(50, startY + gap * 3, 100, 20);
        cardPanel.add(balanceKey);

        JLabel balanceVal = new JLabel("¥ " + member.getBalance());
        balanceVal.setFont(new Font("Arial", Font.BOLD, 16));
        balanceVal.setForeground(StyleUtils.COLOR_DANGER);
        balanceVal.setHorizontalAlignment(SwingConstants.RIGHT);
        balanceVal.setBounds(150, startY + gap * 3, 140, 20);
        cardPanel.add(balanceVal);

        JButton closeBtn = new JButton(LanguageUtils.getText("btn.close"));
        StyleUtils.styleButton(closeBtn, StyleUtils.COLOR_INFO);
        closeBtn.setBounds(50, 340, 245, 40);
        closeBtn.addActionListener(e -> dispose());
        cardPanel.add(closeBtn);
    }

    private void addInfoRow(JPanel panel, String label, String value, int y) {
        JLabel k = new JLabel(label);
        k.setFont(StyleUtils.FONT_NORMAL);
        k.setForeground(StyleUtils.COLOR_INFO);
        k.setBounds(50, y, 100, 20);
        panel.add(k);
        JLabel v = new JLabel(value);
        v.setFont(StyleUtils.FONT_BOLD);
        v.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        v.setBounds(150, y, 140, 20);
        panel.add(v);
    }
}