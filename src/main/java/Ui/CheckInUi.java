package Ui;

import entity.CheckIn;
import entity.Member;
import service.CheckInService;
import service.MemberService;
import utils.LanguageUtils; // 导入
import utils.StyleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class CheckInUi extends JFrame {

    private CheckInService checkInService;
    private MemberService memberService;
    private JTextField inputField;
    private JTextArea resultArea;

    public CheckInUi() {
        this.checkInService = new CheckInService();
        this.memberService = new MemberService();
        StyleUtils.initGlobalTheme();

        setTitle("✅ " + LanguageUtils.getText("checkin.title"));
        setSize(600, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(null);

        initView();
        setVisible(true);
    }

    private void initView() {
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new CheckInUi());
        langBtn.setBounds(500, 10, 70, 30);
        add(langBtn);

        JLabel iconLbl = new JLabel("👋", SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        iconLbl.setBounds(0, 30, 600, 70);
        add(iconLbl);

        JLabel titleLbl = new JLabel(LanguageUtils.getText("checkin.title"), SwingConstants.CENTER);
        titleLbl.setFont(StyleUtils.FONT_TITLE_BIG);
        titleLbl.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        titleLbl.setBounds(0, 100, 600, 40);
        add(titleLbl);

        // 修复：确保 LanguageUtils 中有 checkin.tip
        JLabel tipLbl = new JLabel(LanguageUtils.getText("checkin.tip"), SwingConstants.CENTER);
        tipLbl.setFont(StyleUtils.FONT_NORMAL);
        tipLbl.setForeground(StyleUtils.COLOR_INFO);
        tipLbl.setBounds(0, 160, 600, 20);
        add(tipLbl);

        inputField = new JTextField();
        inputField.setBounds(150, 190, 300, 50);
        inputField.setFont(new Font("Arial", Font.BOLD, 20));
        inputField.setHorizontalAlignment(SwingConstants.CENTER);
        StyleUtils.styleTextField(inputField);
        inputField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) performCheckIn(); }
        });
        add(inputField);

        // 修复：确保 LanguageUtils 中有 checkin.btn
        JButton checkBtn = new JButton(LanguageUtils.getText("checkin.btn"));
        StyleUtils.styleButton(checkBtn, StyleUtils.COLOR_PRIMARY);
        checkBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        checkBtn.setBounds(150, 255, 300, 45);
        checkBtn.addActionListener(e -> performCheckIn());
        add(checkBtn);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(StyleUtils.FONT_NORMAL);
        resultArea.setBackground(new Color(245, 247, 250));
        resultArea.setForeground(StyleUtils.COLOR_INFO);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.setBounds(50, 330, 500, 80);
        scroll.setBorder(BorderFactory.createTitledBorder(LanguageUtils.getText("checkin.log")));
        add(scroll);
    }

    private void performCheckIn() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) { showMsg("⚠️ " + LanguageUtils.getText("msg.incomplete"), false); return; }
        List<Member> list = memberService.search(text);
        if (list.isEmpty()) { showMsg("❌ Not Found: [" + text + "]", false); inputField.selectAll(); return; }

        Member targetMember = list.get(0);
        if (list.size() > 1) {
            // 简单处理多结果，默认选第一个，实际可弹窗
        }

        CheckInService.ServiceResult<CheckIn> result = checkInService.checkIn(targetMember.getId());
        if (result.isSuccess()) {
            showMsg("✅ [" + targetMember.getName() + "] " + result.getMessage(), true);
            inputField.setText(""); inputField.requestFocus();
        } else {
            showMsg("❌ [" + targetMember.getName() + "] " + result.getMessage(), false);
            inputField.selectAll();
        }
    }

    private void showMsg(String msg, boolean success) {
        resultArea.append(String.format("[%tT] %s\n", System.currentTimeMillis(), msg));
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
        inputField.setBackground(success ? Color.WHITE : new Color(255, 235, 235));
    }
}