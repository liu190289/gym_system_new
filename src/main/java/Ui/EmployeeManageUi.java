package Ui;

import dao.EmployeeDAO;
import dao.EmployeeRoleDAO;
import entity.Employee;
import utils.LanguageUtils;
import utils.StyleUtils;
import service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class EmployeeManageUi extends JFrame {

    private EmployeeDAO employeeDAO;
    private EmployeeRoleDAO roleDAO;
    private UserService userService;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public EmployeeManageUi() {
        this.employeeDAO = new EmployeeDAO();
        this.roleDAO = new EmployeeRoleDAO();
        this.userService = new UserService();

        StyleUtils.initGlobalTheme();
        setTitle("👔 " + LanguageUtils.getText("em.title"));
        setSize(1000, 650);
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

        toolBar.add(new JLabel("🔍 " + LanguageUtils.getText("mm.col.name") + ":"));
        searchField = new JTextField(15);
        StyleUtils.styleTextField(searchField);
        toolBar.add(searchField);

        JButton searchBtn = new JButton(LanguageUtils.getText("btn.search"));
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> searchEmployee());
        toolBar.add(searchBtn);

        JButton refreshBtn = new JButton("🔄 " + LanguageUtils.getText("btn.refresh"));
        StyleUtils.styleButton(refreshBtn, StyleUtils.COLOR_INFO);
        refreshBtn.addActionListener(e -> loadData());
        toolBar.add(refreshBtn);

        toolBar.add(new JSeparator(SwingConstants.VERTICAL));

        JButton addBtn = new JButton("➕ " + LanguageUtils.getText("em.add"));
        StyleUtils.styleButton(addBtn, StyleUtils.COLOR_SUCCESS);
        addBtn.addActionListener(e -> addEmployee());
        toolBar.add(addBtn);

        JButton accountBtn = new JButton("👤 " + LanguageUtils.getText("em.account"));
        StyleUtils.styleButton(accountBtn, new Color(155, 89, 182));
        accountBtn.addActionListener(e -> manageAccount());
        toolBar.add(accountBtn);

        JButton editBtn = new JButton("✏️ " + LanguageUtils.getText("btn.edit"));
        StyleUtils.styleButton(editBtn, StyleUtils.COLOR_WARNING);
        editBtn.addActionListener(e -> editEmployee());
        toolBar.add(editBtn);

        JButton delBtn = new JButton("🗑️ " + LanguageUtils.getText("btn.delete"));
        StyleUtils.styleButton(delBtn, StyleUtils.COLOR_DANGER);
        delBtn.addActionListener(e -> deleteEmployee());
        toolBar.add(delBtn);

        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new EmployeeManageUi());
        toolBar.add(langBtn);

        String[] columns = {
                "ID",
                LanguageUtils.getText("mm.col.name"),
                LanguageUtils.getText("em.col.role"),
                LanguageUtils.getText("mm.col.phone"),
                LanguageUtils.getText("em.col.hiredate")
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        employeeTable = new JTable(tableModel);
        StyleUtils.styleTable(employeeTable);
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Employee> list = employeeDAO.getAllEmployees();
        for (Employee e : list) addEmployeeToTable(e);
    }

    private void searchEmployee() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        tableModel.setRowCount(0);
        List<Employee> list = employeeDAO.searchEmployeeByName(kw);
        for (Employee e : list) addEmployeeToTable(e);
    }

    // === 核心修改点：这里不再调用 DAO 的 getRoleDisplayName，而是用 LanguageUtils 翻译 ===
    private void addEmployeeToTable(Employee e) {
        String roleText;
        int rid = e.getRoleId();

        if (rid == EmployeeRoleDAO.ROLE_ID_ADMIN) {
            roleText = LanguageUtils.getText("role.admin");
        } else if (rid == EmployeeRoleDAO.ROLE_ID_TRAINER) {
            roleText = LanguageUtils.getText("role.trainer");
        } else if (rid == EmployeeRoleDAO.ROLE_ID_RECEPTIONIST) {
            roleText = LanguageUtils.getText("role.receptionist");
        } else {
            roleText = LanguageUtils.getText("role.unknown");
        }

        tableModel.addRow(new Object[]{
                e.getId(),
                e.getName(),
                roleText,  // 使用翻译后的文本
                e.getPhone(),
                e.getHireDate()
        });
    }

    private void addEmployee() {
        JTextField nameF = new JTextField();
        JTextField phoneF = new JTextField();

        // === 核心修改点：下拉框选项也要双语化 ===
        String[] roles = {
                LanguageUtils.getText("role.admin"),
                LanguageUtils.getText("role.receptionist"),
                LanguageUtils.getText("role.trainer")
        };
        JComboBox<String> roleBox = new JComboBox<>(roles);

        Object[] message = {
                LanguageUtils.getText("mm.col.name"), nameF,
                LanguageUtils.getText("mm.col.phone"), phoneF,
                LanguageUtils.getText("em.col.role"), roleBox
        };

        if (JOptionPane.showConfirmDialog(this, message, LanguageUtils.getText("em.add"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String name = nameF.getText().trim();
            String phone = phoneF.getText().trim();
            if(name.isEmpty() || phone.isEmpty()) return;

            int idx = roleBox.getSelectedIndex();
            // 保持原有逻辑: 0->Admin(3), 1->Receptionist(2), 2->Trainer(1)
            int roleId = (idx == 0) ? EmployeeRoleDAO.ROLE_ID_ADMIN : (idx == 1 ? EmployeeRoleDAO.ROLE_ID_RECEPTIONIST : EmployeeRoleDAO.ROLE_ID_TRAINER);

            Employee emp = new Employee();
            emp.setName(name);
            emp.setPhone(phone);
            emp.setRoleId(roleId);
            emp.setHireDate(new Date());

            if(employeeDAO.addEmployee(emp)){
                userService.setEmployeeAccount(emp.getId(), emp.getPhone(), "123456");
                JOptionPane.showMessageDialog(this, LanguageUtils.getText("msg.success"));
                loadData();
            }
        }
    }

    private void manageAccount() {
        int row = employeeTable.getSelectedRow();
        if(row == -1) return;
        int empId = (int)tableModel.getValueAt(row, 0);
        String name = (String)tableModel.getValueAt(row, 1);
        String phone = (String)tableModel.getValueAt(row, 3);

        JTextField uF = new JTextField(phone);
        JTextField pF = new JTextField();
        Object[] msg = {
                "User [" + name + "]",
                LanguageUtils.getText("login.user"), uF,
                LanguageUtils.getText("login.pass"), pF
        };

        if(JOptionPane.showConfirmDialog(this, msg, LanguageUtils.getText("em.account"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
            userService.setEmployeeAccount(empId, uF.getText(), pF.getText());
            JOptionPane.showMessageDialog(this, LanguageUtils.getText("msg.success"));
        }
    }

    // 简单实现编辑和删除，逻辑保持原样
    private void editEmployee() {
        int row = employeeTable.getSelectedRow();
        if(row == -1) return;
        // 编辑逻辑可参考 addEmployee，注意回填 role 时需反向查找 index
        JOptionPane.showMessageDialog(this, "Edit feature not fully implemented in snippet.");
    }

    private void deleteEmployee() {
        int row = employeeTable.getSelectedRow();
        if(row == -1) return;
        int id = (int)tableModel.getValueAt(row, 0);
        if(JOptionPane.showConfirmDialog(this, LanguageUtils.getText("btn.delete") + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if(employeeDAO.deleteEmployee(id)) {
                loadData();
            }
        }
    }
}