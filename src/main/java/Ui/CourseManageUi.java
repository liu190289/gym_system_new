package Ui;

import entity.Course;
import entity.Employee;
import service.CourseService;
import utils.LanguageUtils; // 引入
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CourseManageUi extends JFrame {

    private Employee currentUser;
    private CourseService courseService;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public CourseManageUi(Employee user) {
        this.currentUser = user;
        this.courseService = new CourseService();
        StyleUtils.initGlobalTheme();
        setTitle(LanguageUtils.getText("cm.title"));
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(10, 10));
        initView();
        loadData();
        setVisible(true);
    }

    private void initView() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        add(toolBar, BorderLayout.NORTH);

        // 修复：cm.search
        toolBar.add(new JLabel("🔍 " + LanguageUtils.getText("cm.search") + ":"));
        searchField = new JTextField(15);
        StyleUtils.styleTextField(searchField);
        toolBar.add(searchField);

        JButton searchBtn = new JButton(LanguageUtils.getText("btn.search"));
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> searchCourse());
        toolBar.add(searchBtn);

        JButton refreshBtn = new JButton("🔄 " + LanguageUtils.getText("btn.refresh"));
        StyleUtils.styleButton(refreshBtn, StyleUtils.COLOR_INFO);
        refreshBtn.addActionListener(e -> loadData());
        toolBar.add(refreshBtn);

        toolBar.add(new JSeparator(SwingConstants.VERTICAL));

        JButton addBtn = new JButton("➕ " + LanguageUtils.getText("cm.add"));
        StyleUtils.styleButton(addBtn, StyleUtils.COLOR_SUCCESS);
        addBtn.addActionListener(e -> new CourseAddUi(CourseManageUi.this).setVisible(true));
        toolBar.add(addBtn);

        JButton delBtn = new JButton("🗑️ " + LanguageUtils.getText("btn.delete"));
        StyleUtils.styleButton(delBtn, StyleUtils.COLOR_DANGER);
        delBtn.addActionListener(e -> deleteCourse());
        toolBar.add(delBtn);

        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new CourseManageUi(currentUser));
        toolBar.add(langBtn);

        String[] columns = {"ID", LanguageUtils.getText("cm.col.course"), "Type", LanguageUtils.getText("cm.col.trainer"), LanguageUtils.getText("cm.col.time"), "Min", LanguageUtils.getText("cm.col.capacity"), LanguageUtils.getText("cm.col.status")};
        tableModel = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        courseTable = new JTable(tableModel);
        StyleUtils.styleTable(courseTable);
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<Course> list = courseService.getAllCourses();
        for (Course c : list) {
            service.CourseService.CourseDetail detail = courseService.getCourseDetail(c.getCourseId());
            String trainerName = (detail != null) ? detail.getTrainerName() : "?";
            String status = (detail != null && detail.isFull()) ? "🔴 Full" : "🟢 Open";
            tableModel.addRow(new Object[]{c.getCourseId(), c.getName(), c.getType(), trainerName, c.getCourseTime(), c.getDuration(), c.getMaxCapacity(), status});
        }
    }

    private void searchCourse() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { loadData(); return; }
        tableModel.setRowCount(0);
        List<Course> list = courseService.searchByName(keyword);
        for (Course c : list) {
            service.CourseService.CourseDetail detail = courseService.getCourseDetail(c.getCourseId());
            String trainerName = (detail != null) ? detail.getTrainerName() : "?";
            String status = (detail != null && detail.isFull()) ? "🔴 Full" : "🟢 Open";
            tableModel.addRow(new Object[]{c.getCourseId(), c.getName(), c.getType(), trainerName, c.getCourseTime(), c.getDuration(), c.getMaxCapacity(), status});
        }
    }

    private void deleteCourse() {
        int row = courseTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a course!"); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, LanguageUtils.getText("btn.delete") + "?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            courseService.deleteCourse(id);
            loadData();
        }
    }
}