package Ui;

import service.UserService;
import utils.LanguageUtils; // 引入
import utils.StyleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class LoginUi extends JFrame implements MouseListener {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel registerLabel;

    public void LoginJFrame() {
        StyleUtils.initGlobalTheme();
        this.setSize(900, 600);
        this.setTitle(LanguageUtils.getText("app.title") + " - " + LanguageUtils.getText("login.title"));
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.getContentPane().setBackground(StyleUtils.COLOR_BG);

        initView();
        this.setVisible(true);
    }

    private void initView() {
        // 语言切换按钮
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new LoginUi().LoginJFrame());
        langBtn.setBounds(800, 10, 70, 30);
        this.add(langBtn);

        JPanel leftPanel = new JPanel();
        leftPanel.setBounds(0, 0, 400, 600);
        leftPanel.setBackground(StyleUtils.COLOR_PRIMARY);
        leftPanel.setLayout(null);

        JLabel logoText = new JLabel("Gym System");
        logoText.setFont(new Font("Arial", Font.BOLD, 40));
        logoText.setForeground(Color.WHITE);
        logoText.setBounds(50, 200, 300, 50);
        leftPanel.add(logoText);

        JLabel subText = new JLabel(LanguageUtils.getText("login.slogan"));
        subText.setFont(StyleUtils.FONT_NORMAL);
        subText.setForeground(new Color(255, 255, 255, 200));
        subText.setBounds(55, 260, 300, 30);
        leftPanel.add(subText);
        this.add(leftPanel);

        int startX = 500, startY = 120, fieldW = 300, fieldH = 45;

        JLabel titleLbl = new JLabel(LanguageUtils.getText("login.title"));
        titleLbl.setFont(StyleUtils.FONT_TITLE_BIG);
        titleLbl.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        titleLbl.setBounds(startX, startY, 200, 40);
        this.add(titleLbl);

        JLabel uLabel = new JLabel(LanguageUtils.getText("login.user"));
        uLabel.setFont(StyleUtils.FONT_NORMAL);
        uLabel.setForeground(StyleUtils.COLOR_INFO);
        uLabel.setBounds(startX, startY + 60, 200, 30);
        this.add(uLabel);

        usernameField = new JTextField();
        usernameField.setBounds(startX, startY + 90, fieldW, fieldH);
        StyleUtils.styleTextField(usernameField);
        this.add(usernameField);

        JLabel pLabel = new JLabel(LanguageUtils.getText("login.pass"));
        pLabel.setFont(StyleUtils.FONT_NORMAL);
        pLabel.setForeground(StyleUtils.COLOR_INFO);
        pLabel.setBounds(startX, startY + 150, 200, 30);
        this.add(pLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(startX, startY + 180, fieldW, fieldH);
        StyleUtils.styleTextField(passwordField);
        this.add(passwordField);

        loginButton = new JButton(LanguageUtils.getText("login.btn"));
        loginButton.setBounds(startX, startY + 260, fieldW, 50);
        StyleUtils.styleButton(loginButton, StyleUtils.COLOR_PRIMARY);
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
        loginButton.addMouseListener(this);
        this.add(loginButton);

        registerLabel = new JLabel(LanguageUtils.getText("login.reg_link"));
        registerLabel.setFont(StyleUtils.FONT_NORMAL);
        registerLabel.setForeground(StyleUtils.COLOR_PRIMARY);
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLabel.setBounds(startX, startY + 320, 250, 30);
        registerLabel.addMouseListener(this);
        this.add(registerLabel);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == loginButton) {
            handleLogin();
        } else if (e.getSource() == registerLabel) {
            this.dispose();
            new RegisterUi().RegisterJFrame();
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, LanguageUtils.getText("msg.incomplete"));
            return;
        }

        UserService userService = new UserService();
        UserService.LoginResult result = userService.login(username, password);

        if (result.isSuccess()) {
            this.dispose();
            new MainUi(result.getUserType(), result.getUserData());
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), LanguageUtils.getText("msg.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}