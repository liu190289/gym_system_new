package Ui;

import entity.CheckIn;
import entity.Member;
import service.CheckInService;
import service.MemberService;
import utils.LanguageUtils;
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

        // 标题可以稍微改一下，或者保持原样
        setTitle(LanguageUtils.getText("checkin.title"));
        setSize(600, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(null);

        initView();
        setVisible(true);
    }

    private void initView() {
        // 语言切换按钮
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new CheckInUi());
        langBtn.setBounds(500, 10, 70, 30);
        add(langBtn);

        // 图标
        JLabel iconLbl = new JLabel("👋", SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        iconLbl.setBounds(0, 30, 600, 70);
        add(iconLbl);

        // 标题
        JLabel titleLbl = new JLabel(LanguageUtils.getText("checkin.title"), SwingConstants.CENTER);
        titleLbl.setFont(StyleUtils.FONT_TITLE_BIG);
        titleLbl.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        titleLbl.setBounds(0, 100, 600, 40);
        add(titleLbl);

        // 提示文字
        JLabel tipLbl = new JLabel(LanguageUtils.getText("checkin.tip"), SwingConstants.CENTER);
        tipLbl.setFont(StyleUtils.FONT_NORMAL);
        tipLbl.setForeground(StyleUtils.COLOR_INFO);
        tipLbl.setBounds(0, 160, 600, 20);
        add(tipLbl);

        // 输入框
        inputField = new JTextField();
        inputField.setBounds(150, 190, 300, 50);
        inputField.setFont(new Font("Arial", Font.BOLD, 20));
        inputField.setHorizontalAlignment(SwingConstants.CENTER);
        StyleUtils.styleTextField(inputField);

        // 回车键默认触发“签到” (你也可以改成不触发，防止误操作)
        inputField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performCheckIn();
            }
        });
        add(inputField);

        // ==================== 按钮区域修改 ====================

        // 1. 签到按钮 (Check In) - 放在左侧
        JButton checkInBtn = new JButton(LanguageUtils.getText("checkin.btn"));
        StyleUtils.styleButton(checkInBtn, StyleUtils.COLOR_PRIMARY); // 蓝色
        checkInBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        // 原来是宽度300，现在改为145，留10px间距
        checkInBtn.setBounds(150, 255, 145, 45);
        checkInBtn.addActionListener(e -> performCheckIn());
        add(checkInBtn);

        // 2. 签退按钮 (Check Out) - 放在右侧
        // 尝试获取双语文本，如果没有配置 Key，则默认显示 "Check Out"
        String checkOutText = "Check Out";
        try {
            String val = LanguageUtils.getText("checkin.checkout");
            if (val != null && !val.isEmpty() && !val.equals("checkin.checkout")) {
                checkOutText = val;
            }
        } catch (Exception e) {
            // 忽略异常，使用默认英文
        }

        JButton checkOutBtn = new JButton(checkOutText);
        StyleUtils.styleButton(checkOutBtn, StyleUtils.COLOR_WARNING); // 橙色/黄色，区分颜色
        checkOutBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        // x坐标 = 150(左按钮x) + 145(左按钮宽) + 10(间距) = 305
        checkOutBtn.setBounds(305, 255, 145, 45);
        checkOutBtn.addActionListener(e -> performCheckOut());
        add(checkOutBtn);

        // ====================================================

        // 日志区域
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(StyleUtils.FONT_NORMAL);
        resultArea.setBackground(new Color(245, 247, 250));
        resultArea.setForeground(StyleUtils.COLOR_INFO);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.setBounds(50, 330, 500, 80);
        // 如果你有 checkin.log 这个key，就用它，否则用默认字符串
        String logTitle = "Log";
        try { logTitle = LanguageUtils.getText("checkin.log"); } catch(Exception e){}

        scroll.setBorder(BorderFactory.createTitledBorder(logTitle));
        add(scroll);
    }

    // === 签到逻辑 ===
    private void performCheckIn() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) { showMsg("⚠️ Input is empty", false); return; }

        List<Member> list = memberService.search(text);
        if (list.isEmpty()) { showMsg("❌ Not Found: [" + text + "]", false); inputField.selectAll(); return; }

        Member targetMember = list.get(0);

        // 调用 Service
        CheckInService.ServiceResult<CheckIn> result = checkInService.checkIn(targetMember.getId());

        handleResult(result, targetMember, "Check-In");
    }

    // === 签退逻辑 (新增) ===
    private void performCheckOut() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) { showMsg("⚠️ Input is empty", false); return; }

        List<Member> list = memberService.search(text);
        if (list.isEmpty()) { showMsg("❌ Not Found: [" + text + "]", false); inputField.selectAll(); return; }

        Member targetMember = list.get(0);

        // 调用 Service 的 checkOut 方法 (请确保你的 Service 中有这个方法)
        CheckInService.ServiceResult<CheckIn> result = checkInService.checkOut(targetMember.getId());

        handleResult(result, targetMember, "Check-Out");
    }

    // 统一处理结果反馈
    private void handleResult(CheckInService.ServiceResult<CheckIn> result, Member member, String type) {
        if (result.isSuccess()) {
            showMsg("✅ [" + member.getName() + "] " + type + " Success!", true);
            inputField.setText("");
            inputField.requestFocus();
        } else {
            showMsg("❌ [" + member.getName() + "] " + type + " Failed: " + result.getMessage(), false);
            inputField.selectAll();
        }
    }

    private void showMsg(String msg, boolean success) {
        resultArea.append(String.format("[%tT] %s\n", System.currentTimeMillis(), msg));
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
        // 成功显白底，失败显红底提醒
        inputField.setBackground(success ? Color.WHITE : new Color(255, 235, 235));
    }
}