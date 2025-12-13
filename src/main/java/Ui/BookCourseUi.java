package Ui;

import entity.Booking;
import entity.Course;
import entity.Member;
import service.BookingService;
import service.CourseService;
import utils.LanguageUtils; // 引入
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BookCourseUi extends JFrame {

    private Member member;
    private CourseService courseService;
    private BookingService bookingService;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public BookCourseUi(Member member) {
        this.member = member;
        this.courseService = new CourseService();
        this.bookingService = new BookingService();
        StyleUtils.initGlobalTheme();
        setTitle("📅 " + LanguageUtils.getText("book.title"));
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(15, 15));
        initView();
        loadCourses();
        setVisible(true);
    }

    private void initView() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLbl = new JLabel("🔥 " + LanguageUtils.getText("book.hot"));
        titleLbl.setFont(StyleUtils.FONT_TITLE);
        titleLbl.setForeground(StyleUtils.COLOR_TEXT_MAIN);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        searchField = new JTextField(15);
        StyleUtils.styleTextField(searchField);
        JButton searchBtn = new JButton(LanguageUtils.getText("btn.search"));
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> loadCourses());
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new BookCourseUi(member));
        searchPanel.add(langBtn);

        topPanel.add(titleLbl, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        String[] cols = {"ID", LanguageUtils.getText("cm.col.course"), LanguageUtils.getText("cm.col.trainer"), LanguageUtils.getText("cm.col.time"), "Min", LanguageUtils.getText("cm.col.capacity"), LanguageUtils.getText("cm.col.status")};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        courseTable = new JTable(tableModel);
        StyleUtils.styleTable(courseTable);
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        bottomPanel.setBackground(Color.WHITE);
        JButton refreshBtn = new JButton("🔄 " + LanguageUtils.getText("btn.refresh"));
        StyleUtils.styleButton(refreshBtn, StyleUtils.COLOR_INFO);
        refreshBtn.addActionListener(e -> loadCourses());
        JButton bookBtn = new JButton("✅ " + LanguageUtils.getText("book.btn"));
        StyleUtils.styleButton(bookBtn, StyleUtils.COLOR_SUCCESS);
        bookBtn.setPreferredSize(new Dimension(120, 40));
        bookBtn.addActionListener(e -> performBooking());
        bottomPanel.add(refreshBtn);
        bottomPanel.add(bookBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadCourses() {
        tableModel.setRowCount(0);
        String keyword = searchField.getText().trim();
        List<Course> courses = courseService.getAvailableCourses();
        for (Course c : courses) {
            if (!keyword.isEmpty() && !c.getName().contains(keyword)) continue;
            service.CourseService.CourseDetail detail = courseService.getCourseDetail(c.getCourseId());
            String trainerName = (detail != null) ? detail.getTrainerName() : "?";
            int slots = (detail != null) ? detail.getAvailableSlots() : 0;
            tableModel.addRow(new Object[]{c.getCourseId(), c.getName(), trainerName, c.getCourseTime(), c.getDuration(), slots, "🟢 Open"});
        }
    }

    private void performBooking() {
        int row = courseTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a course!"); return; }
        int courseId = (int) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, LanguageUtils.getText("book.btn") + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            BookingService.ServiceResult<Booking> result = bookingService.createAndConfirmBooking(member.getId(), courseId);
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, LanguageUtils.getText("msg.success"));
                loadCourses();
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage());
            }
        }
    }
}