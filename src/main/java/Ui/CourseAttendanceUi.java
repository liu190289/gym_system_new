package Ui;

import dao.EmployeeRoleDAO;
import entity.Booking;
import entity.Course;
import entity.Employee;
import service.BookingService;
import service.CourseService;
import utils.LanguageUtils; // 导入
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

        // === 修复点 1：复选框文本双语化 ===
        showAllCheck = new JCheckBox(LanguageUtils.getText("att.show_history"));
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

        // 中间列表
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        centerPanel.setOpaque(false);

        infoLabel = new JLabel("...", SwingConstants.CENTER);
        infoLabel.setForeground(StyleUtils.COLOR_INFO);
        centerPanel.add(infoLabel, BorderLayout.NORTH);

        String[] cols = {"ID", LanguageUtils.getText("mm.col.name"), LanguageUtils.getText("mm.col.phone"), LanguageUtils.getText("mm.col.status"), "Action"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
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

    private static class CourseItem {
        Course c;
        public CourseItem(Course c) { this.c = c; }
        @Override
        public String toString() {
            String timeStr = (c.getCourseTime() != null) ? utils.DateUtils.formatDateTime(c.getCourseTime()) : "N/A";
            return "【" + timeStr + "】" + c.getName();
        }
    }

    private void loadMyCourses() {
        courseBox.removeAllItems();
        List<Course> list = new ArrayList<>();

        boolean isAdmin = (trainer.getRoleId() == EmployeeRoleDAO.ROLE_ID_ADMIN);

        if (isAdmin) {
            // 管理员加载所有
            list = courseService.getAllCourses();
            // === 修复点 2：提示文本双语化 ===
            infoLabel.setText(LanguageUtils.getText("att.admin_mode") + " (" + list.size() + ")");
        } else {
            // 教练加载自己
            if (showAllCheck.isSelected()) {
                list = courseService.getCoursesByTrainer(trainer.getId());
            } else {
                list = courseService.getTrainerCoursesToday(trainer.getId());
            }

            if (list.isEmpty()) {
                infoLabel.setText(showAllCheck.isSelected() ?
                        "No records found." : "No courses today (Check 'History' to see all).");
            }
        }

        for (Course c : list) {
            courseBox.addItem(new CourseItem(c));
        }

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
        List<Booking> bookings = bookingService.getBookingsByCourse(item.c.getCourseId());

        int count = 0;
        for (Booking b : bookings) {
            if (!BookingService.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                String memberName = "未知";
                String phone = "-";

                try {
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

        // 如果不是管理员模式，才显示选中课程的学生数（防止覆盖掉管理员提示）
        if (!infoLabel.getText().startsWith(LanguageUtils.getText("att.admin_mode").substring(0, 5))) {
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
                loadStudents();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status.");
            }
        }
    }
}