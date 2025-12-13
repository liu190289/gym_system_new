package Ui;

import entity.Member;
import entity.MembershipCard;
import service.MemberService;
import dao.MembershipCardDAO;
import utils.LanguageUtils; // 引入
import utils.StyleUtils;

import javax.swing.*;
import java.awt.*;

public class RenewUi extends JDialog {

    private MemberService memberService;
    private MembershipCardDAO cardDAO;
    private Member currentMember;
    private MembershipCard currentCard;
    private boolean isStaffOperation;
    private JComboBox<String> daysComboBox;
    private JTextField daysField, priceField;
    private JRadioButton balanceRadio, cashRadio;

    public RenewUi(Frame owner, Member member, boolean isStaffOperation) {
        super(owner, LanguageUtils.getText("renew.title"), true);
        this.currentMember = member;
        this.isStaffOperation = isStaffOperation;
        this.memberService = new MemberService();
        this.cardDAO = new MembershipCardDAO();
        this.currentCard = cardDAO.getActiveMembershipCard(member.getId());
        StyleUtils.initGlobalTheme();
        setSize(500, 550);
        setLocationRelativeTo(owner);
        setLayout(null);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        if (currentCard == null) {
            JOptionPane.showMessageDialog(owner, "No active card!", "Warning", JOptionPane.WARNING_MESSAGE);
            dispose(); return;
        }
        initView();
        setVisible(true);
    }

    private void initView() {
        // 语言切换
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new RenewUi((Frame)getParent(), currentMember, isStaffOperation));
        langBtn.setBounds(400, 5, 70, 25);
        add(langBtn);

        int x = 40, w = 400, h = 40, y = 20;
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(20, 30, 445, 90);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        add(infoPanel);

        JLabel nameLbl = new JLabel(LanguageUtils.getText("mm.col.name") + ": " + currentMember.getName());
        nameLbl.setFont(StyleUtils.FONT_BOLD);
        nameLbl.setForeground(StyleUtils.COLOR_PRIMARY);
        nameLbl.setBounds(20, 15, 200, 25);
        infoPanel.add(nameLbl);

        JLabel dateLbl = new JLabel(LanguageUtils.getText("renew.validity") + ": " + currentCard.getEndDate());
        dateLbl.setFont(StyleUtils.FONT_NORMAL);
        dateLbl.setForeground(StyleUtils.COLOR_DANGER);
        dateLbl.setBounds(20, 45, 300, 25);
        infoPanel.add(dateLbl);

        y += 120;
        addLabel(LanguageUtils.getText("renew.duration") + ":", x, y);
        if (isStaffOperation) {
            daysField = new JTextField("30"); StyleUtils.styleTextField(daysField); daysField.setBounds(x + 80, y, 150, h); add(daysField);
        } else {
            daysComboBox = new JComboBox<>(new String[]{"30 Days", "365 Days"});
            daysComboBox.setBounds(x + 80, y, 220, h); daysComboBox.addActionListener(e -> updatePrice()); add(daysComboBox);
        }

        y += 60;
        addLabel(LanguageUtils.getText("renew.price") + ":", x, y);
        priceField = new JTextField(); StyleUtils.styleTextField(priceField); priceField.setBounds(x + 80, y, 150, h);
        if (!isStaffOperation) { priceField.setEditable(false); updatePrice(); } else priceField.setText("200");
        add(priceField);

        y += 60;
        addLabel(LanguageUtils.getText("renew.paytype") + ":", x, y);
        balanceRadio = new JRadioButton(LanguageUtils.getText("renew.balance_pay"));
        balanceRadio.setBounds(x + 80, y, 120, h); balanceRadio.setSelected(true);
        cashRadio = new JRadioButton(LanguageUtils.getText("renew.cash_pay"));
        cashRadio.setBounds(x + 210, y, 120, h);
        ButtonGroup group = new ButtonGroup(); group.add(balanceRadio); group.add(cashRadio);
        add(balanceRadio); if (isStaffOperation) add(cashRadio);

        JButton confirmBtn = new JButton(LanguageUtils.getText("btn.confirm"));
        StyleUtils.styleButton(confirmBtn, StyleUtils.COLOR_SUCCESS);
        confirmBtn.setBounds(40, 430, 400, 50);
        confirmBtn.addActionListener(e -> performRenew());
        add(confirmBtn);
    }

    private void addLabel(String text, int x, int y) {
        JLabel l = new JLabel(text); l.setFont(StyleUtils.FONT_BOLD); l.setBounds(x, y, 80, 40); add(l);
    }

    private void updatePrice() {
        int idx = daysComboBox.getSelectedIndex();
        priceField.setText(idx == 0 ? "200.0" : "1200.0");
    }

    private void performRenew() {
        try {
            int days = isStaffOperation ? Integer.parseInt(daysField.getText()) : (daysComboBox.getSelectedIndex() == 0 ? 30 : 365);
            double price = Double.parseDouble(priceField.getText());
            boolean useBalance = isStaffOperation ? balanceRadio.isSelected() : true;
            if (JOptionPane.showConfirmDialog(this, LanguageUtils.getText("btn.confirm") + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                memberService.renewMembership(currentMember.getId(), days, price, useBalance);
                dispose();
            }
        } catch (Exception e) {}
    }
}