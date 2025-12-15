package Ui;

import dao.EmployeeRoleDAO; // 1. 记得引入这个，用来判断角色
import entity.Booking;
import entity.Course;
import entity.Employee;
import service.BookingService;
import service.CourseService;
import utils.LanguageUtils;
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CourseAttendanceUi extends JFrame {

    private Employee trainer;
    private CourseService courseService;
    private BookingService bookingService;

    // 组件
    private JComboBox<CourseItem> courseBox;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JLabel infoLabel;
    private JCheckBox showAllCheck;

    public CourseAttendanceUi(Employee trainer) {
        this.trainer = trainer;
        this.courseService = new CourseService();
        this.bookingService = new BookingService();

        StyleUtils.initGlobalTheme();
        setTitle("📋 " + LanguageUtils.getText("att.title"));
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(15, 15));

        initView();
        loadMyCourses(); // 默认加载
        setVisible(true);
    }

    private void initView() {
        // ... (这部分 UI 代码不用变，保持原样即可) ...
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        add(topPanel, BorderLayout.NORTH);

        JLabel lbl = new JLabel(LanguageUtils.getText("att.select") + ":");
        lbl.setFont(StyleUtils.FONT_TITLE);
        topPanel.add(lbl);

        courseBox = new JComboBox<>();
        courseBox.setPreferredSize(new Dimension(300, 35));
        courseBox.addActionListener(e -> loadStudents());
        topPanel.add(courseBox);

        showAllCheck = new JCheckBox("显示所有历史课程"); // 也可以改成 "Show All / History"
        showAllCheck.setBackground(Color.WHITE);
        showAllCheck.setFont(StyleUtils.FONT_NORMAL);
        showAllCheck.addActionListener(e -> loadMyCourses());
        topPanel.add(showAllCheck);

        JButton loadBtn = new JButton("📂 " + LanguageUtils.getText("att.load"));
        StyleUtils.styleButton(loadBtn, StyleUtils.COLOR_PRIMARY);
        loadBtn.addActionListener(e -> loadStudents());
        topPanel.add(loadBtn);

        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new CourseAttendanceUi(trainer));
        topPanel.add(langBtn);

        // 中间和底部布局保持不变...
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        centerPanel.setOpaque(false);

        infoLabel = new JLabel("...", SwingConstants.CENTER);
        infoLabel.setForeground(StyleUtils.COLOR_INFO);
        centerPanel.add(infoLabel, BorderLayout.NORTH);

        String[] cols = {"ID", LanguageUtils.getText("mm.col.name"), LanguageUtils.getText("mm.col.phone"), LanguageUtils.getText("mm.col.status"), "Action"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        studentTable = new JTable(tableModel);
        StyleUtils.styleTable(studentTable);

        studentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) checkInStudent();
            }
        });

        JScrollPane scroll = new JScrollPane(studentTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        centerPanel.add(scroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JLabel tipLbl = new JLabel("💡 " + LanguageUtils.getText("att.tip"), SwingConstants.CENTER);
        tipLbl.setFont(StyleUtils.FONT_NORMAL);
        tipLbl.setForeground(StyleUtils.COLOR_INFO);
        tipLbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(tipLbl, BorderLayout.SOUTH);
    }

    // 内部类
    private static class CourseItem {
        Course c;
        public CourseItem(Course c) { this.c = c; }
        @Override
        public String toString() {
            String timeStr = (c.getCourseTime() != null) ? utils.DateUtils.formatDateTime(c.getCourseTime()) : "N/A";
            return "【" + timeStr + "】" + c.getName();
        }
    }

    /**
     * 核心修改逻辑：区分管理员和教练
     */
    private void loadMyCourses() {
        courseBox.removeAllItems();
        List<Course> list = new ArrayList<>();

        // 1. 判断角色
        // 注意：你需要确保 trainer.getRoleId() 能正确获取到角色ID
        // 如果 EmployeeRoleDAO 没导包，也可以直接用数字判断 (Admin通常是1或3，看你数据库定义)
        // 这里假设 ROLE_ID_ADMIN 是你在 EmployeeRoleDAO 里定义的常量
        boolean isAdmin = (trainer.getRoleId() == EmployeeRoleDAO.ROLE_ID_ADMIN);

        if (isAdmin) {
            // === 管理员逻辑：看所有课程 ===
            // 管理员我就不区分“今天”还是“历史”了，或者简单粗暴全部加载
            // 如果你想管理员也支持筛选，可以使用 getAllCourses() 配合日期过滤，这里先直接加载所有
            list = courseService.getAllCourses();
            infoLabel.setText("管理员模式：加载所有课程 (" + list.size() + ")");
        } else {
            // === 教练逻辑：只看自己的课 ===
            if (showAllCheck.isSelected()) {
                list = courseService.getCoursesByTrainer(trainer.getId());
            } else {
                list = courseService.getTrainerCoursesToday(trainer.getId());
            }

            if (list.isEmpty()) {
                infoLabel.setText(showAllCheck.isSelected() ?
                        "您没有任何排课记录。" : "您今天没有课程安排 (勾选'显示所有'查看历史)。");
            }
        }

        // 填充下拉框
        for (Course c : list) {
            courseBox.addItem(new CourseItem(c));
        }

        // 默认选中
        if (!list.isEmpty()) {
            courseBox.setSelectedIndex(0);
            loadStudents();
        } else {
            tableModel.setRowCount(0);
        }
    }

    private void loadStudents() {
        CourseItem item = (CourseItem) courseBox.getSelectedItem();
        if (item == null) return;

        tableModel.setRowCount(0);
        // 使用 courseId 查预定记录
        List<Booking> bookings = bookingService.getBookingsByCourse(item.c.getCourseId());

        int count = 0;
        for (Booking b : bookings) {
            if (!BookingService.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                String memberName = "未知";
                String phone = "-";

                try {
                    // 获取详情
                    service.BookingService.BookingDetail detail = bookingService.getBookingDetail(b.getBookingId());
                    if (detail != null) {
                        memberName = detail.getMemberName();
                        if (detail.getMember() != null) phone = detail.getMember().getPhone();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String status = b.getBookingStatus();
                if (BookingService.STATUS_CONFIRMED.equals(status)) status = "✅ Signed";
                else if (BookingService.STATUS_PENDING.equals(status)) status = "⏳ Pending";

                tableModel.addRow(new Object[]{
                        b.getBookingId(), memberName, phone, status, "Double Click"
                });
                count++;
            }
        }
        // 更新提示文字
        if (!infoLabel.getText().startsWith("管理员")) {
            infoLabel.setText("[" + item.c.getName() + "] Student Count: " + count);
        }
    }

    private void checkInStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) return;

        int bookingId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String status = (String) tableModel.getValueAt(row, 3);

        if (status.contains("Signed") || status.contains("✅")) {
            JOptionPane.showMessageDialog(this, "Already Checked-In!");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, LanguageUtils.getText("btn.confirm") + " Check-In [" + name + "]?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            boolean success = bookingService.confirmBooking(bookingId).isSuccess();
            if (success) {
                // JOptionPane.showMessageDialog(this, "Success!"); // 可选提示
                loadStudents(); // 刷新列表
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status.");
            }
        }
    }
}