package Ui;

import dao.EmployeeRoleDAO;
import entity.Employee;
import entity.Member;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MainUi extends JFrame implements MouseListener {

    // 保存当前登录的用户信息
    private String userType; // "member" 或 "employee"
    private Object userData; // Member 对象 或 Employee 对象

    // --- 通用组件 ---
    private JLabel welcomeLabel;
    private JButton logoutBtn = new JButton("退出登录");

    // --- 会员专属按钮 ---
    private JButton myProfileBtn = new JButton("个人信息");
    private JButton bookCourseBtn = new JButton("预约课程");
    private JButton myBookingsBtn = new JButton("我的预约");
    private JButton buyCardBtn = new JButton("会员卡/续费");

    // --- 员工专属按钮 ---
    // 1. 教练专用
    private JButton courseCheckInBtn = new JButton("上课点名");

    // 2. 前台/管理员通用 (运营功能)
    private JButton checkInBtn = new JButton("进场签到");
    private JButton courseManageBtn = new JButton("排课管理");
    private JButton memberManageBtn = new JButton("会员管理");
    private JButton productManageBtn = new JButton("商品/库存管理");

    // 3. 管理员专用 (人事功能)
    private JButton employeeManageBtn = new JButton("员工/人事管理");

    /**
     * 构造方法
     * @param userType 用户类型 "member" 或 "employee"
     * @param userData 具体的用户对象 (Member 或 Employee)
     */
    public MainUi(String userType, Object userData) {
        this.userType = userType;
        this.userData = userData;
        initView();
    }

    private void initView() {
        // 1. 基础窗口设置
        this.setSize(900, 700);
        this.setTitle("健身房管理系统 - 主页");
        this.setLocationRelativeTo(null); // 居中
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(null); // 空布局

        // 2. 显示欢迎信息
        String name = "用户";
        String roleName = "访客";

        if ("member".equals(userType) && userData instanceof Member) {
            name = ((Member) userData).getName();
            roleName = "会员";
        } else if ("employee".equals(userType) && userData instanceof Employee) {
            name = ((Employee) userData).getName();
            roleName = ((Employee) userData).getRole(); // 获取具体角色(教练/前台/管理员)
        }

        welcomeLabel = new JLabel("欢迎回来，" + name + " [" + roleName + "]");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        welcomeLabel.setBounds(30, 20, 500, 30);
        this.getContentPane().add(welcomeLabel);

        // 3. 添加通用按钮 (退出登录)
        logoutBtn.setBounds(750, 20, 100, 30);
        logoutBtn.setBackground(new Color(255, 100, 100)); // 淡红色
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addMouseListener(this);
        this.getContentPane().add(logoutBtn);

        // 4. 根据用户类型加载不同的菜单按钮
        if ("member".equals(userType)) {
            loadMemberMenu();
        } else if ("employee".equals(userType)) {
            loadEmployeeMenu();
        }

        // 5. 设置背景
        JLabel background = new JLabel();
        background.setBounds(0, 0, 900, 700);
        background.setBackground(new Color(225, 240, 255)); // 淡蓝色背景
        background.setOpaque(true);
        this.getContentPane().add(background);

        this.setVisible(true);
    }

    // ==================== 会员菜单加载 ====================
    private void loadMemberMenu() {
        int startX = 100;
        int startY = 100;
        int btnWidth = 160;
        int btnHeight = 60;
        int gap = 40;

        // --- 第一排 ---
        myProfileBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        myProfileBtn.setBounds(startX, startY, btnWidth, btnHeight);
        myProfileBtn.addMouseListener(this);
        this.getContentPane().add(myProfileBtn);

        bookCourseBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        bookCourseBtn.setBounds(startX + btnWidth + gap, startY, btnWidth, btnHeight);
        bookCourseBtn.addMouseListener(this);
        this.getContentPane().add(bookCourseBtn);

        myBookingsBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        myBookingsBtn.setBounds(startX + (btnWidth + gap) * 2, startY, btnWidth, btnHeight);
        myBookingsBtn.addMouseListener(this);
        this.getContentPane().add(myBookingsBtn);

        // --- 第二排：仅保留续费入口 ---
        JButton myCardBtn = new JButton("会员卡/续费");
        myCardBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        myCardBtn.setBackground(new Color(255, 215, 0)); // 金色
        myCardBtn.setBounds(startX, startY + btnHeight + gap, btnWidth, btnHeight);

        myCardBtn.addActionListener(e -> {
            Member mem = (Member) userData;
            dao.MembershipCardDAO cardDAO = new dao.MembershipCardDAO();
            if (cardDAO.hasMemberValidCard(mem.getId())) {
                // 有卡 -> 续费 (isStaff=false: 只能余额支付)
                new RenewUi(this, mem, false);
            } else {
                // 没卡 -> 提示去前台
                JOptionPane.showMessageDialog(this,
                        "您当前没有有效的会员卡。\n请前往前台柜台办理开卡业务！",
                        "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        this.getContentPane().add(myCardBtn);
    }

    // ==================== 员工菜单加载 (权限分离) ====================
    private void loadEmployeeMenu() {
        if (!(userData instanceof Employee)) return;
        Employee emp = (Employee) userData;
        int roleId = emp.getRoleId();

        int x = 100;
        int y = 120;
        int w = 180;
        int h = 60;
        int gap = 40;

        // --- 角色权限判断 ---

        // 1. 教练权限 (Trainer)
        // 功能：上课点名
        if (roleId == EmployeeRoleDAO.ROLE_ID_TRAINER) {
            addSectionLabel("教练功能", x, y - 40);

            courseCheckInBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
            courseCheckInBtn.setBounds(x, y, w, h);
            courseCheckInBtn.addMouseListener(this);
            this.getContentPane().add(courseCheckInBtn);
        }

        // 2. 前台权限 (Receptionist)
        // 功能：进场签到、排课管理、会员管理
        else if (roleId == EmployeeRoleDAO.ROLE_ID_RECEPTIONIST) {
            addSectionLabel("前台运营", x, y - 40);

            // 第一排
            checkInBtn.setBounds(x, y, w, h);
            checkInBtn.addMouseListener(this);
            this.getContentPane().add(checkInBtn);

            courseManageBtn.setBounds(x + w + gap, y, w, h);
            courseManageBtn.addMouseListener(this);
            this.getContentPane().add(courseManageBtn);

            memberManageBtn.setBounds(x + (w + gap) * 2, y, w, h);
            memberManageBtn.addMouseListener(this);
            this.getContentPane().add(memberManageBtn);
            // 第二排：增值业务 (商品售卖)
            int y2 = y + h + gap; // 下移一行

            JButton shopBtn = new JButton("商品售卖 (POS)");
            shopBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
            shopBtn.setBackground(new Color(255, 230, 200)); // 淡橙色高亮
            shopBtn.setBounds(x, y2, w, h); // 放在第二排第一个
            shopBtn.addActionListener(e -> new ShopUi());
            this.getContentPane().add(shopBtn);

            // >>> 新增：余额充值 (放在商品售卖旁边) <<<
            JButton rechargeBtn = new JButton("会员充值 (Cash)");
            rechargeBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
            rechargeBtn.setBackground(new Color(144, 238, 144)); // 浅绿色，代表进账
            rechargeBtn.setBounds(x + w + gap, y2, w, h);
            rechargeBtn.addActionListener(e -> new RechargeUi()); // 点击打开充值界面
            this.getContentPane().add(rechargeBtn);

            // >>> 新增：库存管理按钮 <<<
            productManageBtn.setBounds(x + w + gap, y2, w, h); // 放在第二排第二个
            productManageBtn.addMouseListener(this); // 关联事件
            this.getContentPane().add(productManageBtn);


            // 第三排：会籍业务 (新增)
            int y3 = y2 + h + gap;

            // 新会员开卡
            JButton openCardBtn = new JButton("新会员开卡");
            openCardBtn.setBackground(new Color(255, 215, 0)); // 金色
            openCardBtn.setBounds(x, y3, w, h);
            openCardBtn.addActionListener(e -> handleStaffCardAction("buy"));
            this.getContentPane().add(openCardBtn);

            // 老会员续费
            JButton staffRenewBtn = new JButton("会员续费");
            staffRenewBtn.setBackground(new Color(255, 215, 0));
            staffRenewBtn.setBounds(x + w + gap, y3, w, h);
            staffRenewBtn.addActionListener(e -> handleStaffCardAction("renew"));
            this.getContentPane().add(staffRenewBtn);


        }

        // 3. 管理员权限 (Admin)
        // 功能：所有功能 + 员工管理
        else if (roleId == EmployeeRoleDAO.ROLE_ID_ADMIN) {
            addSectionLabel("综合管理 (管理员)", x, y - 40);

            // === 第一排：基础运营 (y) ===
            // 1. 进场签到
            checkInBtn.setBounds(x, y, w, h);
            checkInBtn.addMouseListener(this);
            this.getContentPane().add(checkInBtn);

            // 2. 排课管理
            courseManageBtn.setBounds(x + w + gap, y, w, h);
            courseManageBtn.addMouseListener(this);
            this.getContentPane().add(courseManageBtn);

            // 3. 会员管理
            memberManageBtn.setBounds(x + (w + gap) * 2, y, w, h);
            memberManageBtn.addMouseListener(this);
            this.getContentPane().add(memberManageBtn);

            // === 第二排：内部管理 (y2) ===
            int y2 = y + h + gap;

            // 1. 上课点名
            courseCheckInBtn.setText("上课点名 (代教)");
            courseCheckInBtn.setBounds(x, y2, w, h);
            courseCheckInBtn.addMouseListener(this);
            this.getContentPane().add(courseCheckInBtn);

            // 2. 员工管理
            employeeManageBtn.setText("员工/人事管理");
            employeeManageBtn.setBackground(new Color(255, 150, 150)); // 淡红
            employeeManageBtn.setBounds(x + w + gap, y2, w, h);
            employeeManageBtn.addMouseListener(this);
            this.getContentPane().add(employeeManageBtn);

            // 3. 库存管理
            productManageBtn.setText("商品/库存管理");
            productManageBtn.setBackground(new Color(249, 126, 11)); // 橙色
            productManageBtn.setBounds(x + (w + gap) * 2, y2, w, h);
            productManageBtn.addMouseListener(this);
            this.getContentPane().add(productManageBtn);

            // === 第三排：收银与会籍 (y3) ===
            int y3 = y2 + h + gap;

            // 1. 商品售卖
            JButton shopBtn = new JButton("商品售卖 (POS)");
            shopBtn.setBounds(x, y3, w, h);
            shopBtn.addActionListener(e -> new ShopUi());
            this.getContentPane().add(shopBtn);

            // 2. 余额充值
            JButton rechargeBtn = new JButton("余额充值 (Cash)");
            rechargeBtn.setBackground(new Color(144, 238, 144)); // 浅绿
            rechargeBtn.setBounds(x + w + gap, y3, w, h);
            rechargeBtn.addActionListener(e -> new RechargeUi());
            this.getContentPane().add(rechargeBtn);

            // 3. 开卡/续费 (合并入口)
            JButton cardOpBtn = new JButton("开卡/续费办理");
            cardOpBtn.setBackground(new Color(255, 215, 0)); // 金色
            cardOpBtn.setBounds(x + (w + gap) * 2, y3, w, h);
            cardOpBtn.addActionListener(e -> {
                String[] options = {"新会员开卡", "老会员续费"};
                int choice = JOptionPane.showOptionDialog(this, "请选择业务类型:", "会籍业务",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                if (choice == 0) handleStaffCardAction("buy");
                if (choice == 1) handleStaffCardAction("renew");
            });
            this.getContentPane().add(cardOpBtn);

            // === 第四排：数据报表 (y4) ===
            int y4 = y3 + h + gap;

            // 1. 经营数据报表
            JButton reportBtn = new JButton("经营数据报表");
            reportBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
            reportBtn.setBackground(new Color(100, 149, 237)); // 矢车菊蓝
            reportBtn.setForeground(Color.WHITE);
            // 让它宽一点，或者放在第一个位置
            reportBtn.setBounds(x, y4, w, h);
            reportBtn.addActionListener(e -> new ReportUi());
            this.getContentPane().add(reportBtn);
        }
    }

    // 辅助方法：添加小标题
    private void addSectionLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.BOLD, 16));
        label.setForeground(Color.GRAY);
        label.setBounds(x, y, 200, 30);
        this.getContentPane().add(label);
    }

    // ==================== 事件处理 ====================
    @Override
    public void mouseClicked(MouseEvent e) {
        // --- 退出登录 ---
        if (e.getSource() == logoutBtn) {
            int confirm = JOptionPane.showConfirmDialog(this, "确定要退出吗？", "退出", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose(); // 关闭主界面
                new LoginUi().LoginJFrame(); // 返回登录界面
            }
        }

        // --- 会员功能 ---
        else if (e.getSource() == myProfileBtn) {
            if (userData instanceof Member) {
                new Ui.InfoUi((Member) userData).setVisible(true);
            }
        } else if (e.getSource() == bookCourseBtn) {
            if (userData instanceof Member) {
                new BookCourseUi((Member) userData);
            }
        } else if (e.getSource() == myBookingsBtn) {
            if (userData instanceof Member) {
                new Ui.MyBookingUi((Member) userData);
            }
        }
        // --- 员工功能 ---
        // 1. 进场签到 (前台/管理员)
        else if (e.getSource() == checkInBtn) {
            new CheckInUi();
        }
        // 2. 排课管理 (前台/管理员)
        else if (e.getSource() == courseManageBtn) {
            if (userData instanceof Employee) {
                new Ui.CourseManageUi((Employee) userData);
            }
        }
        // 3. 会员管理 (前台/管理员)
        else if (e.getSource() == memberManageBtn) {
            new Ui.MemberManageUi();
        }
        // 4. 上课点名 (教练/管理员)
        else if (e.getSource() == courseCheckInBtn) {
            if (userData instanceof Employee) {
                new Ui.CourseAttendanceUi((Employee) userData);
            }
        }
        // 5. 员工管理 (管理员独有)
        else if (e.getSource() == employeeManageBtn) {
            new Ui.EmployeeManageUi();
        }
        // >>> 新增：商品/库存管理事件 <<<
        else if (e.getSource() == productManageBtn) {
            new ProductManageUi(); // 点击打开库存管理界面
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    // ========== 辅助方法：处理员工的开卡/续费逻辑 ==========
    // 避免代码重复，把搜索逻辑提出来
    private void handleStaffCardAction(String actionType) {
        String input = JOptionPane.showInputDialog(this, "请输入会员手机号或ID:");
        if (input == null || input.trim().isEmpty()) return;

        service.MemberService ms = new service.MemberService();
        java.util.List<Member> list = ms.search(input);

        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "未找到该会员！请先在「会员管理」中注册。");
            return;
        }

        Member targetMember = list.get(0); // 默认选第一个

        if ("buy".equals(actionType)) {
            // 开卡逻辑
            dao.MembershipCardDAO cardDAO = new dao.MembershipCardDAO();
            if (cardDAO.hasMemberValidCard(targetMember.getId())) {
                JOptionPane.showMessageDialog(this, "该会员已有有效卡！请使用续费功能。");
            } else {
                new Ui.BuyCardUi(targetMember); // 打开开卡界面
            }
        } else if ("renew".equals(actionType)) {
            // 续费逻辑 (isStaff = true)
            new RenewUi(this, targetMember, true);
        }
    }
}

