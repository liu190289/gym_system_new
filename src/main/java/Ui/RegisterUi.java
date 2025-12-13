package Ui;

import com.toedter.calendar.JDateChooser;
import entity.Member;
import service.MemberService;
import service.UserService;
import utils.LanguageUtils;
import utils.LoginUtils;
import utils.StyleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

public class RegisterUi extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private JPasswordField confirmPassField;
    private JTextField phoneField;
    private JTextField emailField;
    private JComboBox<String> genderBox;
    private JDateChooser birthdayChooser;
    private JTextField codeField;
    private JLabel codeImageLbl;
    private String currentCode;

    public void RegisterJFrame() {
        StyleUtils.initGlobalTheme();
        this.setSize(550, 750);
        this.setTitle("💪 " + LanguageUtils.getText("app.title") + " - " + LanguageUtils.getText("reg.title"));
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.getContentPane().setBackground(StyleUtils.COLOR_BG);

        initView();
        refreshCode();
        this.setVisible(true);
    }

    private void initView() {
        // 语言切换按钮
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new RegisterUi().RegisterJFrame());
        langBtn.setBounds(450, 10, 70, 30);
        this.add(langBtn);

        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 450, 80);
        headerPanel.setBackground(Color.WHITE);
        this.add(headerPanel);

        JLabel titleLbl = new JLabel("📝 " + LanguageUtils.getText("reg.title"));
        titleLbl.setFont(StyleUtils.FONT_TITLE_BIG);
        titleLbl.setForeground(StyleUtils.COLOR_PRIMARY);
        titleLbl.setBounds(40, 20, 300, 40);
        headerPanel.add(titleLbl);

        JLabel subLbl = new JLabel(LanguageUtils.getText("reg.subtitle"));
        subLbl.setFont(StyleUtils.FONT_NORMAL);
        subLbl.setForeground(StyleUtils.COLOR_INFO);
        subLbl.setBounds(45, 55, 300, 20);
        headerPanel.add(subLbl);

        JPanel formPanel = new JPanel(null);
        formPanel.setBounds(40, 100, 455, 580);
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        this.add(formPanel);

        int x = 40, y = 30, w = 375, h = 40, gap = 65;

        addLabel(formPanel, LanguageUtils.getText("reg.user"), x, y);
        userField = new JTextField();
        userField.setBounds(x, y + 25, w, h);
        StyleUtils.styleTextField(userField);
        formPanel.add(userField);

        y += gap;
        addLabel(formPanel, LanguageUtils.getText("reg.pass"), x, y);
        passField = new JPasswordField();
        passField.setBounds(x, y + 25, w, h);
        StyleUtils.styleTextField(passField);
        formPanel.add(passField);

        y += gap;
        addLabel(formPanel, LanguageUtils.getText("reg.confirm"), x, y);
        confirmPassField = new JPasswordField();
        confirmPassField.setBounds(x, y + 25, w, h);
        StyleUtils.styleTextField(confirmPassField);
        formPanel.add(confirmPassField);

        y += gap;
        addLabel(formPanel, LanguageUtils.getText("reg.phone"), x, y);
        phoneField = new JTextField();
        phoneField.setBounds(x, y + 25, 175, h);
        StyleUtils.styleTextField(phoneField);
        formPanel.add(phoneField);

        addLabel(formPanel, LanguageUtils.getText("reg.email"), x + 200, y);
        emailField = new JTextField();
        emailField.setBounds(x + 200, y + 25, 175, h);
        StyleUtils.styleTextField(emailField);
        formPanel.add(emailField);

        y += gap;
        addLabel(formPanel, LanguageUtils.getText("reg.gender"), x, y);
        genderBox = new JComboBox<>(new String[]{LanguageUtils.getText("mm.gender.male"), LanguageUtils.getText("mm.gender.female")});
        genderBox.setBounds(x, y + 25, 100, h);
        genderBox.setFont(StyleUtils.FONT_NORMAL);
        genderBox.setBackground(Color.WHITE);
        formPanel.add(genderBox);

        addLabel(formPanel, LanguageUtils.getText("reg.birth"), x + 120, y);
        birthdayChooser = new JDateChooser();
        birthdayChooser.setBounds(x + 120, y + 25, 255, h);
        birthdayChooser.setDateFormatString("yyyy-MM-dd");
        styleDateChooser(birthdayChooser);
        formPanel.add(birthdayChooser);

        y += gap;
        addLabel(formPanel, LanguageUtils.getText("reg.code"), x, y);
        codeField = new JTextField();
        codeField.setBounds(x, y + 25, 150, h);
        StyleUtils.styleTextField(codeField);
        formPanel.add(codeField);

        codeImageLbl = new JLabel("ABCD");
        codeImageLbl.setBounds(x + 170, y + 25, 100, h);
        codeImageLbl.setOpaque(true);
        codeImageLbl.setBackground(new Color(240, 248, 255));
        codeImageLbl.setFont(new Font("Monospaced", Font.BOLD | Font.ITALIC, 24));
        codeImageLbl.setForeground(Color.BLUE);
        codeImageLbl.setHorizontalAlignment(SwingConstants.CENTER);
        codeImageLbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        codeImageLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        codeImageLbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { refreshCode(); }
        });
        formPanel.add(codeImageLbl);

        y += gap + 10;
        JButton registerBtn = new JButton(LanguageUtils.getText("reg.btn"));
        registerBtn.setBounds(x, y, w, 50);
        StyleUtils.styleButton(registerBtn, StyleUtils.COLOR_PRIMARY);
        registerBtn.setFont(new Font("微软雅黑", Font.BOLD, 18));
        registerBtn.addActionListener(e -> performRegister());
        formPanel.add(registerBtn);

        JLabel backLabel = new JLabel(LanguageUtils.getText("reg.back"));
        backLabel.setHorizontalAlignment(SwingConstants.CENTER);
        backLabel.setFont(StyleUtils.FONT_NORMAL);
        backLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLabel.setBounds(x, y + 60, w, 30);
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new LoginUi().LoginJFrame();
            }
        });
        formPanel.add(backLabel);
    }

    private void styleDateChooser(JDateChooser dateChooser) {
        if(dateChooser.getDateEditor().getUiComponent() instanceof JTextField) {
            JTextField dateEditor = (JTextField) dateChooser.getDateEditor().getUiComponent();
            StyleUtils.styleTextField(dateEditor);
            dateEditor.setBorder(null);
        }
        for (Component comp : dateChooser.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                btn.setIcon(null);
                btn.setText("📅");
                btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
            }
        }
        dateChooser.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(220, 223, 230), 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        dateChooser.setBackground(Color.WHITE);
    }

    private void addLabel(JPanel panel, String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("微软雅黑", Font.BOLD, 12));
        lbl.setForeground(StyleUtils.COLOR_INFO);
        lbl.setBounds(x, y, 200, 20);
        panel.add(lbl);
    }

    private void refreshCode() {
        this.currentCode = LoginUtils.generateVerificationCode();
        codeImageLbl.setText(currentCode);
    }

    private void performRegister() {
        String name = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        String confirmPass = new String(confirmPassField.getPassword()).trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String genderStr = (String) genderBox.getSelectedItem();
        Date birth = birthdayChooser.getDate();
        String inputCode = codeField.getText().trim();

        if (name.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() ||
                phone.isEmpty() || email.isEmpty() || birth == null || inputCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, LanguageUtils.getText("msg.incomplete"), "Tip", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!pass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", LanguageUtils.getText("msg.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!inputCode.equalsIgnoreCase(currentCode)) {
            JOptionPane.showMessageDialog(this, "Invalid Code!", LanguageUtils.getText("msg.error"), JOptionPane.ERROR_MESSAGE);
            refreshCode();
            codeField.setText("");
            return;
        }

        String gender = LanguageUtils.getText("mm.gender.male").equals(genderStr) ? "male" : "female";
        MemberService memberService = new MemberService();

        // >>> 修复点 1：使用 service.ServiceResult (独立类) <<<
        service.ServiceResult<Member> memResult = memberService.register(name, phone, email, gender, birth);

        if (memResult.isSuccess()) {
            UserService userService = new UserService();
            int memberId = memResult.getData().getId();

            // >>> 修复点 2：使用 UserService.ServiceResult (内部类) <<<
            UserService.ServiceResult<Void> userResult = userService.registerMemberUser(memberId, name, pass);

            if (userResult.isSuccess()) {
                JOptionPane.showMessageDialog(this, "🎉 " + LanguageUtils.getText("msg.success"));
                this.dispose();
                new LoginUi().LoginJFrame();
            } else {
                JOptionPane.showMessageDialog(this, "Member created but User failed: " + userResult.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Register Failed: " + memResult.getMessage(), LanguageUtils.getText("msg.error"), JOptionPane.ERROR_MESSAGE);
        }
    }
}