package utils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class LanguageUtils {

    private static boolean isChinese = true;
    private static final Map<String, String> CN = new HashMap<>();
    private static final Map<String, String> EN = new HashMap<>();

    static {
        // === 公共 Common ===
        put("app.title", "健身房管理系统", "Gym System");
        put("btn.confirm", "确定", "Confirm");
        put("btn.cancel", "取消", "Cancel");
        put("btn.save", "保存", "Save");
        put("btn.search", "查询", "Search");
        put("btn.refresh", "刷新", "Refresh");
        put("btn.add", "新增", "Add");
        put("btn.edit", "编辑", "Edit");
        put("btn.delete", "删除", "Delete");
        put("btn.close", "关闭", "Close");
        put("msg.success", "操作成功", "Success");
        put("msg.error", "错误", "Error");
        put("msg.incomplete", "请填写完整信息", "Incomplete Info");

        // === 1. 登录 & 注册 ===
        put("login.title", "欢迎登录", "Welcome");
        put("login.slogan", "专业的健身房管理专家", "Professional Gym Expert");
        put("login.user", "账号", "Username");
        put("login.pass", "密码", "Password");
        put("login.btn", "立即登录", "Login");
        put("login.reg_link", "<html><u>没有账号？点此注册会员</u></html>", "<html><u>No account? Sign up here</u></html>");
        put("reg.title", "注册新账号", "Register");
        put("reg.subtitle", "加入我们，开启健康生活", "Join us for a healthy life");
        put("reg.user", "用户名", "Username");
        put("reg.pass", "登录密码", "Password");
        put("reg.confirm", "确认密码", "Confirm Pwd");
        put("reg.phone", "手机号码", "Phone");
        put("reg.email", "电子邮箱", "Email");
        put("reg.gender", "性别", "Gender");
        put("reg.birth", "出生日期", "Birth Date");
        put("reg.code", "验证码", "Code");
        put("reg.btn", "立即注册", "Sign Up");
        put("reg.back", "<html>已有账号？<u style='color:#409EFF'>返回登录</u></html>", "<html>Have account? <u style='color:#409EFF'>Login</u></html>");

        // === 2. 主页 Main ===
        put("main.title", "健身房智能管理系统 Pro", "Gym Management Pro");
        put("main.welcome", "欢迎，", "Welcome, ");
        put("main.logout", "退出", "Logout");
        put("main.exit_confirm", "确定退出吗?", "Are you sure to logout?");
        // 区域 Section
        put("main.sec.service", "我的服务", "My Services");
        put("main.sec.trainer", "教练工作台", "Trainer Workspace");
        put("main.sec.front", "前台高频业务", "Front Desk");
        put("main.sec.member", "会籍与会员服务", "Membership Services");
        put("main.sec.backend", "后台管理", "Backend Mgmt");
        put("main.sec.admin", "综合管理控制台", "Admin Console");
        // 菜单 Menu
        put("menu.profile", "个人信息", "Profile");
        put("menu.book", "预约课程", "Book Course");
        put("menu.mybook", "我的预约", "My Bookings");
        put("menu.card", "会员卡/续费", "Membership");
        put("menu.att", "上课点名", "Attendance");
        put("menu.checkin", "进场签到", "Check-In");
        put("menu.shop", "商品售卖", "POS/Shop");
        put("menu.recharge", "余额充值", "Recharge");
        put("menu.mm", "会员管理", "Members");
        put("menu.newcard", "新会员开卡", "New Card");
        put("menu.renew", "会员续费", "Renew");
        put("menu.cm", "排课管理", "Courses");
        put("menu.stock", "库存管理", "Inventory");
        put("menu.emp", "员工/人事", "HR/Employees");
        put("menu.report", "经营报表", "Reports");

        // === 3. 签到 CheckIn (修复: checkin.tip, checkin.btn) ===
        put("checkin.title", "会员进场签到", "Member Check-In");
        put("checkin.tip", "支持输入：会员ID / 姓名 / 手机号", "Input: ID / Name / Phone");
        put("checkin.btn", "搜索并签到", "Check-In Now");
        put("checkin.log", "操作日志", "Log");
        put("checkin.checkout","签退", "Check-Out");

        // === 4. 课程管理 & 添加 (修复: cm.search) ===
        put("cm.title", "课程排期管理", "Course Scheduling");
        put("cm.search", "课程搜索", "Search Course");
        put("cm.add", "发布新课程", "Add Course");
        put("cm.col.course", "课程名称", "Course Name");
        put("cm.col.trainer", "教练", "Trainer");
        put("cm.col.time", "上课时间", "Time");
        put("cm.col.capacity", "容量", "Capacity");
        put("cm.col.status", "状态", "Status");
        // Course Add Dialog
        put("ca.title", "排课信息录入", "New Course Details");
        put("ca.name", "课程名称", "Course Name");
        put("ca.type", "课程类型", "Course Type");
        put("ca.trainer", "授课教练", "Trainer");
        put("ca.date", "上课日期", "Date");
        put("ca.time", "时间", "Time");
        put("ca.duration", "时长 (分钟)", "Duration (min)");
        put("ca.capacity", "最大人数", "Capacity");
        put("ca.btn", "确认发布", "Publish");

        // === 5. 员工管理 (修复: em.add, em.account) ===
        put("em.title", "员工/人事档案", "HR Management");
        put("em.add", "入职登记", "New Hire");
        put("em.account", "账号管理", "Account Mgmt");
        put("em.col.role", "角色", "Role");
        put("em.col.hiredate", "入职日期", "Hire Date");
        // === 角色名称 Roles (新增) ===
        put("role.admin", "管理员", "Admin");
        put("role.trainer", "健身教练", "Trainer");
        put("role.receptionist", "前台接待", "Receptionist");
        put("role.unknown", "未知角色", "Unknown");

        // === 6. 上课点名 (修复: att.select, att.load) ===
        put("att.title", "上课点名系统", "Class Attendance");
        put("att.select", "选择当前课程", "Select Course");
        put("att.load", "加载名单", "Load List");
        put("att.tip", "双击学生行可进行签到", "Double click to check-in student");
        put("att.show_history", "显示所有历史课程", "Show History Courses");
        put("att.admin_mode", "管理员模式：加载所有课程", "Admin Mode: All Courses Loaded");

        // === 7. 库存管理 & 商城 (修复: shop.col.name, pm.add, pm.edit) ===
        put("pm.title", "商品库存管理", "Inventory Management");
        put("pm.add", "新增商品", "Add Product");
        put("pm.edit", "修改信息", "Edit Info");
        put("pm.del", "下架/删除", "Delete");
        // Shop UI
        put("shop.title", "收银台 (POS)", "Cashier (POS)");
        put("shop.search_ph", "商品名称", "Product Name");
        put("shop.search_btn", "搜索商品", "Search");
        put("shop.show_all", "显示全部", "Show All");
        put("shop.cart_title", "购物车清单", "Cart");
        put("shop.clear", "清空", "Clear");
        put("shop.remove", "删除选中", "Remove");
        put("shop.total", "总计: ", "Total: ");
        put("shop.checkout", "立即结账", "Checkout");
        put("shop.col.name", "商品名称", "Product");
        put("shop.col.price", "单价", "Price");
        put("shop.col.stock", "库存", "Stock");
        put("shop.col.qty", "数量", "Qty");

        // === 8. 余额充值 ===
        put("recharge.title", "会员余额充值", "Balance Recharge");
        put("recharge.step1", "第一步：搜索会员", "Step 1: Search Member");
        put("recharge.step2", "第二步：充值金额", "Step 2: Amount");
        put("recharge.btn", "确认充值", "Confirm");
        put("recharge.balance", "当前余额", "Current Balance");

        // === 9. 报表 Report ===
        put("report.title", "经营数据分析仪表盘", "Business Dashboard");
        put("report.revenue", "总营收", "Total Revenue");
        put("report.members", "会员总数", "Total Members");
        put("report.orders", "今日订单", "Today's Orders");
        put("report.stock", "库存预警", "Low Stock");
        put("report.tab.table", "详细报表", "Table");
        put("report.tab.bar", "营收柱状图", "Bar Chart");
        put("report.tab.pie", "占比饼状图", "Pie Chart");
        // === [新增] 图表内部文字 Charts ===
        put("chart.bar.title", "近7天营收趋势", "Revenue Trend (7 Days)");
        put("chart.bar.x", "日期", "Date");
        put("chart.bar.y", "金额 (¥)", "Amount (¥)");
        put("chart.series.revenue", "营收", "Revenue"); // 柱状图的图例

        put("chart.pie.title", "商品销量分类占比", "Product Sales Distribution");
        put("chart.pie.other", "其他", "Others"); // 饼图如果分类太多的归类
        // === [新增] 图表数据分类 (用于翻译数据库里的原始数据) ===
        // Key 建议用 cat. (Category) 开头
        put("cat.product", "商品售卖", "Product Sales");
        put("cat.renew", "续费业务", "Renewals");
        put("cat.membership", "新办会员", "New Membership");
        put("cat.recharge", "余额充值", "Balance Recharge");
        put("cat.other", "其他", "Others");

        // === 10. 会员管理 Member Manage ===
        put("mm.title", "会员档案管理", "Member Management");
        put("mm.search_lbl", "搜索:", "Search:");
        put("mm.col.id", "ID", "ID");
        put("mm.col.name", "姓名", "Name");
        put("mm.col.phone", "手机号", "Phone");
        put("mm.col.gender", "性别", "Gender");
        put("mm.col.date", "注册时间", "Reg Date");
        put("mm.col.status", "状态", "Status");
        put("mm.col.balance", "余额(¥)", "Balance");
        put("mm.dialog.add", "新增会员", "Add Member");
        put("mm.dialog.edit", "编辑会员", "Edit Member");
        put("mm.gender.male", "男", "Male");
        put("mm.gender.female", "女", "Female");

        // === 其他 Others ===
        put("info.title", "个人档案", "Profile");
        put("info.phone", "手机号", "Phone");
        put("info.email", "邮箱", "Email");
        put("info.gender", "性别", "Gender");
        put("info.balance", "账户余额", "Balance");
        put("book.title", "预约课程", "Book Course");
        put("book.hot", "热门课程预约", "Hot Courses");
        put("book.btn", "立即预约", "Book Now");
        put("mybook.title", "我的预约记录", "My Bookings");
        put("mybook.col.status", "当前状态", "Status");
        put("mybook.tip", "双击记录取消预约", "Double click to cancel");
        put("renew.title", "会员续费", "Renewal");
        put("renew.validity", "有效期至", "Valid Until");
        put("renew.duration", "续费时长", "Duration");
        put("renew.price", "应付金额", "Amount");
        put("renew.paytype", "支付方式", "Payment");
        put("renew.balance_pay", "余额支付", "Balance Pay");
        put("renew.cash_pay", "现金/其它", "Cash/Other");
        put("buy.title", "办理会员卡", "Buy Membership");
        put("buy.subtitle", "选择您的会员方案", "Select Plan");
        put("buy.month", "月卡", "Monthly");
        put("buy.year", "年卡", "Yearly");
        put("buy.btn", "立即开通", "Activate");
    }

    private static void put(String key, String cn, String en) {
        CN.put(key, cn);
        EN.put(key, en);
    }

    public static String getText(String key) {
        return isChinese ? CN.getOrDefault(key, key) : EN.getOrDefault(key, key);
    }

    public static void toggle() {
        isChinese = !isChinese;
    }

    public static JButton createLanguageButton(Window window, Runnable reopener) {
        JButton btn = new JButton(isChinese ? "English" : "中文");
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            toggle();
            window.dispose();
            reopener.run();
        });
        return btn;
    }
}