package Ui;

import dao.EmployeeRoleDAO;
import entity.Employee;
import entity.Member;
import utils.LanguageUtils;
import utils.StyleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainUi extends JFrame {

    private String userType;
    private Object userData;
    private JLabel timeLbl;
    private Timer timer;

    // === 布局常量配置 ===
    private final int WIN_WIDTH = 1000;
    private final int BTN_W = 180;   // 按钮宽度
    private final int BTN_H = 100;   // 按钮高度 (稍微加高)
    private final int GAP_X = 40;    // 水平间距
    private final int GAP_Y = 140;   // 垂直行距 (包含标题空间)
    private final int START_Y = 150; // 第一行按钮的起始Y坐标

    public MainUi(String userType, Object userData) {
        this.userType = userType;
        this.userData = userData;
        StyleUtils.initGlobalTheme();
        initView();
    }

    private void initView() {
        this.setSize(WIN_WIDTH, 750);
        this.setTitle("💪 " + LanguageUtils.getText("main.title"));
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(null);
        this.getContentPane().setBackground(StyleUtils.COLOR_BG);

        // ================= 顶部导航栏 =================
        JPanel header = new JPanel(null);
        header.setBounds(0, 0, WIN_WIDTH, 70);
        header.setBackground(Color.WHITE);
        this.getContentPane().add(header);

        // Logo
        JLabel logo = new JLabel("🏋️ Gym System");
        logo.setFont(StyleUtils.FONT_TITLE_BIG);
        logo.setForeground(StyleUtils.COLOR_PRIMARY);
        logo.setBounds(20, 15, 220, 40);
        header.add(logo);

        // 时间显示
        timeLbl = new JLabel();
        timeLbl.setFont(new Font("Monospaced", Font.BOLD, 16));
        timeLbl.setForeground(new Color(100, 100, 100));
        timeLbl.setBounds(250, 20, 200, 30);
        header.add(timeLbl);
        startClock();

        // 用户信息
        String welcomeText = LanguageUtils.getText("main.welcome");
        if ("member".equals(userType) && userData instanceof Member) {
            welcomeText += ((Member) userData).getName();
        } else if ("employee".equals(userType) && userData instanceof Employee) {
            welcomeText += ((Employee) userData).getName();
        }
        JLabel userLbl = new JLabel(welcomeText);
        userLbl.setFont(StyleUtils.FONT_NORMAL);
        userLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        userLbl.setBounds(460, 20, 240, 30);
        header.add(userLbl);

        // 语言切换
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new MainUi(userType, userData));
        langBtn.setBounds(710, 18, 80, 35);
        header.add(langBtn);

        // 退出
        JButton logoutBtn = new JButton(LanguageUtils.getText("main.logout") + " ❌");
        StyleUtils.styleButton(logoutBtn, StyleUtils.COLOR_DANGER);
        logoutBtn.setBounds(800, 18, 160, 35);
        logoutBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, LanguageUtils.getText("main.exit_confirm"), LanguageUtils.getText("main.logout"), JOptionPane.YES_NO_OPTION) == 0) {
                if (timer != null) timer.stop();
                dispose();
                new LoginUi().LoginJFrame();
            }
        });
        header.add(logoutBtn);

        JSeparator sep = new JSeparator();
        sep.setBounds(0, 70, WIN_WIDTH, 1);
        sep.setForeground(Color.LIGHT_GRAY);
        this.getContentPane().add(sep);

        // ================= 加载菜单 =================
        if ("member".equals(userType)) {
            loadMemberMenu();
        } else if ("employee".equals(userType)) {
            loadEmployeeMenu();
        }

        this.setVisible(true);
    }

    private void startClock() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        timeLbl.setText(sdf.format(new Date()));
        timer = new Timer(1000, e -> timeLbl.setText(sdf.format(new Date())));
        timer.start();
    }

    // ==================== 1. 会员菜单 (3列布局) ====================
    private void loadMemberMenu() {
        // 计算居中起始X: 3个按钮的总宽度
        int startX = calculateStartX(3);
        int y = START_Y;

        // --- 第一行 ---
        addSectionTitle(LanguageUtils.getText("main.sec.service"), startX, y - 35);

        createMenuBtn(LanguageUtils.getText("menu.profile"), "👤", StyleUtils.COLOR_PRIMARY, startX, y,
                () -> new Ui.InfoUi((Member) userData).setVisible(true));

        createMenuBtn(LanguageUtils.getText("menu.book"), "📅", StyleUtils.COLOR_SUCCESS, startX + BTN_W + GAP_X, y,
                () -> new BookCourseUi((Member) userData));

        createMenuBtn(LanguageUtils.getText("menu.mybook"), "📋", StyleUtils.COLOR_WARNING, startX + (BTN_W + GAP_X) * 2, y,
                () -> new Ui.MyBookingUi((Member) userData));

        // --- 第二行 ---
        y += GAP_Y;
        createMenuBtn(LanguageUtils.getText("menu.card"), "💳", StyleUtils.COLOR_DANGER, startX, y, () -> {
            Member mem = (Member) userData;
            dao.MembershipCardDAO cardDAO = new dao.MembershipCardDAO();
            if (cardDAO.hasMemberValidCard(mem.getId())) {
                new RenewUi(this, mem, false);
            } else {
                JOptionPane.showMessageDialog(this, "No Valid Card!", "Tip", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    // ==================== 2. 员工菜单 (布局重构) ====================
    private void loadEmployeeMenu() {
        if (!(userData instanceof Employee)) return;
        Employee emp = (Employee) userData;
        int roleId = emp.getRoleId();

        // ------------------ 教练视图 (居中单列或少列) ------------------
        if (roleId == EmployeeRoleDAO.ROLE_ID_TRAINER) {
            int startX = calculateStartX(3); // 保持与前台一致的左边距
            int y = START_Y;

            addSectionTitle(LanguageUtils.getText("main.sec.trainer"), startX, y - 35);
            createMenuBtn(LanguageUtils.getText("menu.att"), "📝", StyleUtils.COLOR_PRIMARY, startX, y,
                    () -> new Ui.CourseAttendanceUi((Employee) userData));
        }

        // ------------------ 前台视图 (3列布局) ------------------
        else if (roleId == EmployeeRoleDAO.ROLE_ID_RECEPTIONIST) {
            int startX = calculateStartX(3);
            int y = START_Y;

            // Row 1: 前台高频 (签到、收银、充值)
            addSectionTitle(LanguageUtils.getText("main.sec.front"), startX, y - 35);
            createMenuBtn(LanguageUtils.getText("menu.checkin"), "✅", StyleUtils.COLOR_PRIMARY, startX, y, () -> new CheckInUi());
            createMenuBtn(LanguageUtils.getText("menu.shop"), "🛒", StyleUtils.COLOR_WARNING, startX + BTN_W + GAP_X, y, () -> new ShopUi());
            createMenuBtn(LanguageUtils.getText("menu.recharge"), "💰", StyleUtils.COLOR_SUCCESS, startX + (BTN_W + GAP_X) * 2, y, () -> new RechargeUi());

            // Row 2: 会籍服务
            y += GAP_Y;
            addSectionTitle(LanguageUtils.getText("main.sec.member"), startX, y - 35);
            createMenuBtn(LanguageUtils.getText("menu.mm"), "📂", StyleUtils.COLOR_INFO, startX, y, () -> new Ui.MemberManageUi());
            createMenuBtn(LanguageUtils.getText("menu.newcard"), "🆕", StyleUtils.COLOR_DANGER, startX + BTN_W + GAP_X, y, () -> handleStaffCardAction("buy"));
            createMenuBtn(LanguageUtils.getText("menu.renew"), "🔄", StyleUtils.COLOR_DANGER, startX + (BTN_W + GAP_X) * 2, y, () -> handleStaffCardAction("renew"));

            // Row 3: 后台管理
            y += GAP_Y;
            addSectionTitle(LanguageUtils.getText("main.sec.backend"), startX, y - 35);
            createMenuBtn(LanguageUtils.getText("menu.cm"), "📅", StyleUtils.COLOR_INFO, startX, y, () -> new Ui.CourseManageUi((Employee) userData));
            createMenuBtn(LanguageUtils.getText("menu.stock"), "📊", StyleUtils.COLOR_INFO, startX + BTN_W + GAP_X, y, () -> new ProductManageUi());
        }

        // ------------------ 管理员视图 (4列布局 - 优化展示更多功能) ------------------
        else if (roleId == EmployeeRoleDAO.ROLE_ID_ADMIN) {
            int startX = calculateStartX(4); // 管理员功能多，用4列
            int y = START_Y;
            int col2 = startX + BTN_W + GAP_X;
            int col3 = startX + (BTN_W + GAP_X) * 2;
            int col4 = startX + (BTN_W + GAP_X) * 3;

            // Row 1: 核心管理 (签到, 排课, 会员, 员工)
            addSectionTitle(LanguageUtils.getText("main.sec.admin"), startX, y - 35);
            createMenuBtn(LanguageUtils.getText("menu.checkin"), "✅", StyleUtils.COLOR_PRIMARY, startX, y, () -> new CheckInUi());
            createMenuBtn(LanguageUtils.getText("menu.cm"), "📅", StyleUtils.COLOR_PRIMARY, col2, y, () -> new Ui.CourseManageUi((Employee) userData));
            createMenuBtn(LanguageUtils.getText("menu.mm"), "👥", StyleUtils.COLOR_PRIMARY, col3, y, () -> new Ui.MemberManageUi());
            createMenuBtn(LanguageUtils.getText("menu.emp"), "👔", StyleUtils.COLOR_DANGER, col4, y, () -> new Ui.EmployeeManageUi());

            // Row 2: 业务运营 (点名, 库存, 商品, 充值)
            y += GAP_Y;
            createMenuBtn(LanguageUtils.getText("menu.att"), "📝", StyleUtils.COLOR_INFO, startX, y, () -> new Ui.CourseAttendanceUi((Employee) userData));
            createMenuBtn(LanguageUtils.getText("menu.stock"), "📦", StyleUtils.COLOR_WARNING, col2, y, () -> new ProductManageUi());
            createMenuBtn(LanguageUtils.getText("menu.shop"), "🛒", StyleUtils.COLOR_SUCCESS, col3, y, () -> new ShopUi());
            createMenuBtn(LanguageUtils.getText("menu.recharge"), "💰", StyleUtils.COLOR_SUCCESS, col4, y, () -> new RechargeUi());

            // Row 3: 决策与开卡
            y += GAP_Y;
            createMenuBtn(LanguageUtils.getText("menu.card"), "💳", StyleUtils.COLOR_DANGER, startX, y, () -> {
                Object[] options = {LanguageUtils.getText("menu.newcard"), LanguageUtils.getText("menu.renew")};
                int choice = JOptionPane.showOptionDialog(this, "Select:", "Card", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                if (choice == 0) handleStaffCardAction("buy");
                if (choice == 1) handleStaffCardAction("renew");
            });

            createMenuBtn(LanguageUtils.getText("menu.report"), "📊", new Color(100, 100, 255), col2, y, () -> new ReportUi());
        }
    }

    // === 辅助方法 ===

    /**
     * 计算居中布局的起始 X 坐标
     * @param numButtons 列数
     */
    private int calculateStartX(int numButtons) {
        int totalWidth = numButtons * BTN_W + (numButtons - 1) * GAP_X;
        return (WIN_WIDTH - totalWidth) / 2;
    }

    private void createMenuBtn(String text, String icon, Color color, int x, int y, Runnable action) {
        String html = "<html><center><font size='6'>" + icon + "</font><br><font size='4'>" + text + "</font></center></html>";
        JButton btn = new JButton(html);
        btn.setBounds(x, y, BTN_W, BTN_H); // 使用常量大小
        StyleUtils.styleButton(btn, color);

        // 增加阴影或悬停效果 (StyleUtils里已经有了，这里增加动态变色)
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(color.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(color); }
        });

        btn.addActionListener(e -> action.run());
        this.getContentPane().add(btn);
    }

    private void addSectionTitle(String title, int x, int y) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("微软雅黑", Font.BOLD, 16));
        lbl.setForeground(Color.GRAY);
        lbl.setBounds(x, y, 300, 30);
        this.getContentPane().add(lbl);
    }

    private void handleStaffCardAction(String actionType) {
        String input = JOptionPane.showInputDialog(this, "Enter ID/Phone:");
        if (input == null || input.trim().isEmpty()) return;
        service.MemberService ms = new service.MemberService();
        java.util.List<Member> list = ms.search(input);
        if (list.isEmpty()) { JOptionPane.showMessageDialog(this, "Not Found!"); return; }
        Member targetMember = list.get(0);
        if ("buy".equals(actionType)) {
            dao.MembershipCardDAO cardDAO = new dao.MembershipCardDAO();
            if (cardDAO.hasMemberValidCard(targetMember.getId())) JOptionPane.showMessageDialog(this, "Already has card!");
            else new Ui.BuyCardUi(targetMember);
        } else if ("renew".equals(actionType)) new RenewUi(this, targetMember, true);
    }
}