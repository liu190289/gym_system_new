package Ui;

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
    private JCheckBox showAllCheck; // 新增：切换显示全部

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
        loadMyCourses(); // 默认加载今天的
        setVisible(true);
    }

    private void initView() {
        // === 顶部选择栏 ===
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

        // 新增：切换查看历史
        showAllCheck = new JCheckBox("显示所有历史课程");
        showAllCheck.setBackground(Color.WHITE);
        showAllCheck.setFont(StyleUtils.FONT_NORMAL);
        showAllCheck.addActionListener(e -> loadMyCourses());
        topPanel.add(showAllCheck);

        JButton loadBtn = new JButton("📂 " + LanguageUtils.getText("att.load"));
        StyleUtils.styleButton(loadBtn, StyleUtils.COLOR_PRIMARY);
        loadBtn.addActionListener(e -> loadStudents());
        topPanel.add(loadBtn);

        // 语言切换
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new CourseAttendanceUi(trainer));
        topPanel.add(langBtn);

        // === 中间学生列表 ===
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

        // 双击操作
        studentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) checkInStudent();
            }
        });

        JScrollPane scroll = new JScrollPane(studentTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        centerPanel.add(scroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // === 底部提示 ===
        JLabel tipLbl = new JLabel("💡 " + LanguageUtils.getText("att.tip"), SwingConstants.CENTER);
        tipLbl.setFont(StyleUtils.FONT_NORMAL);
        tipLbl.setForeground(StyleUtils.COLOR_INFO);
        tipLbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(tipLbl, BorderLayout.SOUTH);
    }

    // 内部类：ComboBox Item
    private static class CourseItem {
        Course c;
        public CourseItem(Course c) { this.c = c; }
        @Override
        public String toString() {
            // 显示时间，方便区分
            String timeStr = (c.getCourseTime() != null) ? utils.DateUtils.formatDateTime(c.getCourseTime()) : "N/A";
            return "【" + timeStr + "】" + c.getName();
        }
    }

    /**
     * 加载课程列表
     * 逻辑：根据 CheckBox 状态决定加载“今日课程”还是“所有课程”
     */
    private void loadMyCourses() {
        courseBox.removeAllItems();
        List<Course> list;

        if (showAllCheck.isSelected()) {
            // 加载所有 (旧逻辑)
            list = courseService.getCoursesByTrainer(trainer.getId());
        } else {
            // 加载今日 (新逻辑)
            list = courseService.getTrainerCoursesToday(trainer.getId());
        }

        for (Course c : list) {
            courseBox.addItem(new CourseItem(c));
        }

        if (list.isEmpty()) {
            if (showAllCheck.isSelected()) {
                infoLabel.setText("您没有任何排课记录。");
            } else {
                infoLabel.setText("您今天没有课程安排 (勾选'显示所有'查看历史)。");
            }
            tableModel.setRowCount(0);
        } else {
            // 默认选中第一个并加载学生
            courseBox.setSelectedIndex(0);
            loadStudents();
        }
    }

    private void loadStudents() {
        CourseItem item = (CourseItem) courseBox.getSelectedItem();
        if (item == null) return;

        tableModel.setRowCount(0);
        List<Booking> bookings = bookingService.getBookingsByCourse(item.c.getCourseId());

        int count = 0;
        for (Booking b : bookings) {
            // 只显示未取消的
            if (!BookingService.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                // 获取详情 (需确保 BookingService 有 getBookingDetail 方法)
                // 这里假设您之前的 BookingService 代码没变
                // 如果编译报错，请检查 BookingService 是否有 getBookingDetail
                // 或者手动去 memberService 查 member
                String memberName = "未知";
                String phone = "-";

                // 尝试获取详情
                try {
                    // 如果您保留了我之前的 BookingService 完整代码，这行可用：
                    service.BookingService.BookingDetail detail = bookingService.getBookingDetail(b.getBookingId());
                    if (detail != null) {
                        memberName = detail.getMemberName();
                        if (detail.getMember() != null) phone = detail.getMember().getPhone();
                    }
                } catch (Exception e) {
                    // 容错处理
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
        infoLabel.setText("[" + item.c.getName() + "] Student Count: " + count);
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
            bookingService.confirmBooking(bookingId);
            loadStudents(); // 刷新
        }
    }
}