package Ui;

import dao.MembershipCardDAO;
import entity.Member;
import service.MemberService;
import utils.LanguageUtils; // 引入
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BuyCardUi extends JFrame {

    private Member member;
    private MemberService memberService;
    private int selectedType = -1;
    private JPanel monthlyPanel, yearlyPanel;

    public BuyCardUi(Member member) {
        this.member = member;
        this.memberService = new MemberService();
        StyleUtils.initGlobalTheme();
        setTitle("💳 " + LanguageUtils.getText("buy.title"));
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(null);
        initView();
        setVisible(true);
    }

    private void initView() {
        // 语言切换
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new BuyCardUi(member));
        langBtn.setBounds(600, 10, 70, 25);
        add(langBtn);

        JLabel titleLbl = new JLabel(LanguageUtils.getText("buy.subtitle"), SwingConstants.CENTER);
        titleLbl.setFont(StyleUtils.FONT_TITLE_BIG);
        titleLbl.setBounds(0, 30, 700, 40);
        add(titleLbl);

        int cardY = 120, cardW = 240, cardH = 260, gap = 60;
        int startX = (700 - (cardW * 2 + gap)) / 2;

        monthlyPanel = createCardPanel(LanguageUtils.getText("buy.month"), "¥ 200", "30 Days", "Trial", startX, cardY, cardW, cardH);
        monthlyPanel.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { selectCard(MembershipCardDAO.TYPE_MONTHLY); } });
        add(monthlyPanel);

        yearlyPanel = createCardPanel(LanguageUtils.getText("buy.year"), "¥ 1200", "365 Days", "Best Value", startX + cardW + gap, cardY, cardW, cardH);
        yearlyPanel.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { selectCard(MembershipCardDAO.TYPE_YEARLY); } });
        add(yearlyPanel);

        JButton confirmBtn = new JButton(LanguageUtils.getText("buy.btn"));
        StyleUtils.styleButton(confirmBtn, StyleUtils.COLOR_PRIMARY);
        confirmBtn.setBounds(250, 410, 200, 45);
        confirmBtn.addActionListener(e -> performBuy());
        add(confirmBtn);
    }

    private JPanel createCardPanel(String title, String price, String desc1, String desc2, int x, int y, int w, int h) {
        JPanel p = new JPanel(null);
        p.setBounds(x, y, w, h);
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel tLbl = new JLabel(title, SwingConstants.CENTER); tLbl.setFont(StyleUtils.FONT_TITLE); tLbl.setBounds(0, 20, w, 30); p.add(tLbl);
        JLabel pLbl = new JLabel(price, SwingConstants.CENTER); pLbl.setFont(new Font("Arial", Font.BOLD, 36)); pLbl.setForeground(StyleUtils.COLOR_DANGER); pLbl.setBounds(0, 60, w, 50); p.add(pLbl);
        return p;
    }

    private void selectCard(int type) {
        selectedType = type;
        monthlyPanel.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        yearlyPanel.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        if (type == MembershipCardDAO.TYPE_MONTHLY) monthlyPanel.setBorder(new LineBorder(StyleUtils.COLOR_PRIMARY, 3));
        else yearlyPanel.setBorder(new LineBorder(StyleUtils.COLOR_PRIMARY, 3));
    }

    private void performBuy() {
        if (selectedType == -1) return;
        if (JOptionPane.showConfirmDialog(this, LanguageUtils.getText("btn.confirm") + "?", "Pay", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            memberService.buyCard(member.getId(), selectedType);
            dispose();
        }
    }
}