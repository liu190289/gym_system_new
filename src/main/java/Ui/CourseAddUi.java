package Ui;

import com.toedter.calendar.JDateChooser;
import entity.Course;
import entity.Employee;
import service.CourseService;
import service.EmployeeService;
import utils.LanguageUtils; // 引入
import utils.StyleUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CourseAddUi extends JFrame {

    private CourseManageUi parentUi;
    private CourseService courseService;
    private EmployeeService employeeService;
    private JTextField nameField;
    private JComboBox<String> typeBox;
    private JComboBox<TrainerItem> trainerBox;
    private JDateChooser dateChooser;
    private JSpinner timeSpinner;
    private JTextField durationField;
    private JTextField capacityField;

    public CourseAddUi(CourseManageUi parent) {
        this.parentUi = parent;
        this.courseService = new CourseService();
        this.employeeService = new EmployeeService();
        StyleUtils.initGlobalTheme();
        setTitle("📝 " + LanguageUtils.getText("cm.add"));
        setSize(500, 650);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(null);
        initView();
        loadTrainers();
        setVisible(true);
    }

    private void initView() {
        JPanel formPanel = new JPanel(null);
        formPanel.setBounds(30, 30, 425, 540);
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        add(formPanel);

        // 语言切换
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new CourseAddUi(parentUi));
        langBtn.setBounds(380, 5, 60, 20);
        add(langBtn);

        JLabel titleLbl = new JLabel(LanguageUtils.getText("ca.title"), SwingConstants.CENTER);
        titleLbl.setFont(StyleUtils.FONT_TITLE);
        titleLbl.setForeground(StyleUtils.COLOR_PRIMARY);
        titleLbl.setBounds(0, 20, 425, 30);
        formPanel.add(titleLbl);

        int x = 40, y = 70, w = 345, h = 40, gap = 70;

        addLabel(formPanel, LanguageUtils.getText("ca.name"), x, y - 25);
        nameField = new JTextField();
        StyleUtils.styleTextField(nameField);
        nameField.setBounds(x, y, w, h);
        formPanel.add(nameField);

        y += gap;
        addLabel(formPanel, LanguageUtils.getText("ca.type"), x, y - 25);
        String[] types = {CourseService.TYPE_YOGA, CourseService.TYPE_SPINNING, CourseService.TYPE_PILATES, CourseService.TYPE_AEROBICS, CourseService.TYPE_STRENGTH, CourseService.TYPE_OTHER};
        typeBox = new JComboBox<>(types);
        typeBox.setBackground(Color.WHITE);
        typeBox.setBounds(x, y, w, h);
        formPanel.add(typeBox);

        y += gap;
        addLabel(formPanel, LanguageUtils.getText("ca.trainer"), x, y - 25);
        trainerBox = new JComboBox<>();
        trainerBox.setBackground(Color.WHITE);
        trainerBox.setBounds(x, y, w, h);
        formPanel.add(trainerBox);

        y += gap;
        addLabel(formPanel, LanguageUtils.getText("ca.date"), x, y - 25);
        addLabel(formPanel, LanguageUtils.getText("ca.time"), x + 200, y - 25);
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setBounds(x, y, 190, h);
        // 手动移除 DateChooser 默认组件边框
        if(dateChooser.getDateEditor().getUiComponent() instanceof JTextField) {
            ((JTextField)dateChooser.getDateEditor().getUiComponent()).setBorder(BorderFactory.createEmptyBorder());
        }
        dateChooser.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        formPanel.add(dateChooser);

        SpinnerDateModel model = new SpinnerDateModel();
        timeSpinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(editor);
        timeSpinner.setValue(new Date());
        timeSpinner.setBounds(x + 200, y, 145, h);
        formPanel.add(timeSpinner);

        y += gap;
        addLabel(formPanel, LanguageUtils.getText("ca.duration"), x, y - 25);
        addLabel(formPanel, LanguageUtils.getText("ca.capacity"), x + 180, y - 25);
        durationField = new JTextField("60");
        StyleUtils.styleTextField(durationField);
        durationField.setBounds(x, y, 160, h);
        formPanel.add(durationField);
        capacityField = new JTextField("20");
        StyleUtils.styleTextField(capacityField);
        capacityField.setBounds(x + 180, y, 165, h);
        formPanel.add(capacityField);

        y += gap + 10;
        JButton submitBtn = new JButton(LanguageUtils.getText("ca.btn"));
        StyleUtils.styleButton(submitBtn, StyleUtils.COLOR_PRIMARY);
        submitBtn.setBounds(x, y, w, 45);
        submitBtn.addActionListener(e -> performAdd());
        formPanel.add(submitBtn);
    }

    private void addLabel(JPanel p, String txt, int x, int y) {
        JLabel l = new JLabel(txt);
        l.setFont(StyleUtils.FONT_NORMAL);
        l.setForeground(StyleUtils.COLOR_INFO);
        l.setBounds(x, y, 200, 20);
        p.add(l);
    }

    private static class TrainerItem {
        Employee emp;
        public TrainerItem(Employee emp) { this.emp = emp; }
        @Override public String toString() { return emp.getName() + " (ID:" + emp.getId() + ")"; }
    }

    private void loadTrainers() {
        List<Employee> list = employeeService.getAllEmployees();
        for (Employee e : list) {
            if (e.getRoleId() == dao.EmployeeRoleDAO.ROLE_ID_TRAINER) {
                trainerBox.addItem(new TrainerItem(e));
            }
        }
    }

    private void performAdd() {
        String name = nameField.getText().trim();
        String type = (String) typeBox.getSelectedItem();
        TrainerItem trainerItem = (TrainerItem) trainerBox.getSelectedItem();
        Date date = dateChooser.getDate();
        Date time = (Date) timeSpinner.getValue();

        if (name.isEmpty() || trainerItem == null || date == null) {
            JOptionPane.showMessageDialog(this, LanguageUtils.getText("msg.incomplete"));
            return;
        }

        try {
            int duration = Integer.parseInt(durationField.getText().trim());
            int capacity = Integer.parseInt(capacityField.getText().trim());
            Calendar calDate = Calendar.getInstance(); calDate.setTime(date);
            Calendar calTime = Calendar.getInstance(); calTime.setTime(time);
            calDate.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
            calDate.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
            Date finalDate = calDate.getTime();

            CourseService.ServiceResult<Course> result = courseService.createCourse(name, type, duration, capacity, trainerItem.emp.getId(), finalDate);
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, LanguageUtils.getText("msg.success"));
                if (parentUi != null) parentUi.loadData();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed: " + result.getMessage());
            }
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Number Format Error"); }
    }
}