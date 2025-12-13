package Ui;

import com.toedter.calendar.JDateChooser;
import dao.MemberDAO; // 仍然需要 MemberDAO 可能是为了 searchField 的一些兼容，或者可以移除如果完全用 Service
import entity.Member;
import service.MemberService;
import service.ServiceResult; // 核心修复：引入独立的 ServiceResult
import utils.LanguageUtils;
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class MemberManageUi extends JFrame {

    private MemberService memberService;
    private JTable memberTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public MemberManageUi() {
        this.memberService = new MemberService();

        // 1. 初始化主题
        StyleUtils.initGlobalTheme();

        // 设置标题 (从词典获取)
        setTitle("👥 " + LanguageUtils.getText("mm.title"));
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
        // ==================== 顶部工具栏 ====================
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(toolBar, BorderLayout.NORTH);

        // 搜索区域
        JLabel searchLbl = new JLabel("🔍 " + LanguageUtils.getText("mm.search_lbl"));
        searchLbl.setFont(StyleUtils.FONT_NORMAL);
        toolBar.add(searchLbl);

        searchField = new JTextField(15);
        StyleUtils.styleTextField(searchField);
        // 回车搜索
        searchField.addActionListener(e -> searchMember());
        toolBar.add(searchField);

        JButton searchBtn = new JButton(LanguageUtils.getText("btn.search"));
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> searchMember());
        toolBar.add(searchBtn);

        JButton refreshBtn = new JButton("🔄 " + LanguageUtils.getText("btn.refresh"));
        StyleUtils.styleButton(refreshBtn, StyleUtils.COLOR_INFO);
        refreshBtn.addActionListener(e -> loadData());
        toolBar.add(refreshBtn);

        // 分隔线
        toolBar.add(new JSeparator(SwingConstants.VERTICAL));

        // 操作按钮
        JButton addBtn = new JButton("➕ " + LanguageUtils.getText("btn.add"));
        StyleUtils.styleButton(addBtn, StyleUtils.COLOR_SUCCESS);
        addBtn.addActionListener(e -> addMember());
        toolBar.add(addBtn);

        JButton editBtn = new JButton("✏️ " + LanguageUtils.getText("btn.edit"));
        StyleUtils.styleButton(editBtn, StyleUtils.COLOR_WARNING);
        editBtn.addActionListener(e -> editMember());
        toolBar.add(editBtn);

        JButton delBtn = new JButton("🗑️ " + LanguageUtils.getText("btn.delete"));
        StyleUtils.styleButton(delBtn, StyleUtils.COLOR_DANGER);
        delBtn.addActionListener(e -> deleteMember());
        toolBar.add(delBtn);

        // 语言切换按钮
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new MemberManageUi());
        toolBar.add(langBtn);

        // ==================== 中间表格区域 ====================
        String[] columns = {
                LanguageUtils.getText("mm.col.id"),      // ID
                LanguageUtils.getText("mm.col.name"),    // 姓名
                LanguageUtils.getText("mm.col.phone"),   // 手机号
                LanguageUtils.getText("mm.col.gender"),  // 性别
                LanguageUtils.getText("mm.col.date"),    // 注册时间
                LanguageUtils.getText("mm.col.status"),  // 状态
                LanguageUtils.getText("mm.col.balance")  // 余额
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        memberTable = new JTable(tableModel);
        StyleUtils.styleTable(memberTable);

        // 滚动条包裹
        JScrollPane scrollPane = new JScrollPane(memberTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Member> members = memberService.getAllMembers();
        for (Member m : members) {
            addMemberToTable(m);
        }
    }

    private void searchMember() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        tableModel.setRowCount(0);
        List<Member> members = memberService.search(keyword);
        for (Member m : members) {
            addMemberToTable(m);
        }
    }

    private void addMemberToTable(Member m) {
        String genderShow = "male".equals(m.getGender()) ?
                LanguageUtils.getText("mm.gender.male") :
                LanguageUtils.getText("mm.gender.female");

        tableModel.addRow(new Object[]{
                m.getId(),
                m.getName(),
                m.getPhone(),
                genderShow,
                m.getRegisterDate(),
                m.getStatus(),
                String.format("%.2f", m.getBalance())
        });
    }

    // ==================== 新增会员 ====================
    private void addMember() {
        JDialog dialog = new JDialog(this, LanguageUtils.getText("mm.dialog.add"), true);
        dialog.setSize(400, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 20));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        formPanel.setBackground(Color.WHITE);

        JTextField nameField = new JTextField();
        StyleUtils.styleTextField(nameField);

        JTextField phoneField = new JTextField();
        StyleUtils.styleTextField(phoneField);

        JTextField emailField = new JTextField();
        StyleUtils.styleTextField(emailField);

        String[] genders = {LanguageUtils.getText("mm.gender.male"), LanguageUtils.getText("mm.gender.female")};
        JComboBox<String> genderCombo = new JComboBox<>(genders);
        genderCombo.setBackground(Color.WHITE);

        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new Date());
        dateChooser.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        formPanel.add(new JLabel(LanguageUtils.getText("mm.col.name") + ":")); formPanel.add(nameField);
        formPanel.add(new JLabel(LanguageUtils.getText("mm.col.phone") + ":")); formPanel.add(phoneField);
        formPanel.add(new JLabel(LanguageUtils.getText("reg.email") + ":")); formPanel.add(emailField);
        formPanel.add(new JLabel(LanguageUtils.getText("mm.col.gender") + ":")); formPanel.add(genderCombo);
        formPanel.add(new JLabel(LanguageUtils.getText("reg.birth") + ":")); formPanel.add(dateChooser);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        btnPanel.setBackground(Color.WHITE);

        JButton saveBtn = new JButton(LanguageUtils.getText("btn.save"));
        StyleUtils.styleButton(saveBtn, StyleUtils.COLOR_SUCCESS);

        JButton cancelBtn = new JButton(LanguageUtils.getText("btn.cancel"));
        StyleUtils.styleButton(cancelBtn, StyleUtils.COLOR_INFO);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String genderStr = (String) genderCombo.getSelectedItem();
            String gender = LanguageUtils.getText("mm.gender.male").equals(genderStr) ? "male" : "female";
            Date birthDate = dateChooser.getDate();

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || birthDate == null) {
                JOptionPane.showMessageDialog(dialog, LanguageUtils.getText("msg.incomplete"));
                return;
            }

            // 修复点：使用 ServiceResult 而不是 MemberService.ServiceResult
            ServiceResult<Member> result = memberService.register(name, phone, email, gender, birthDate);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(dialog, "🎉 " + LanguageUtils.getText("msg.success"));
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, result.getMessage(), LanguageUtils.getText("msg.error"), JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ==================== 编辑会员 ====================
    private void editMember() {
        int row = memberTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a member first!");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);

        Member member = memberService.getMemberById(id);
        if (member == null) {
            JOptionPane.showMessageDialog(this, "Member not found!");
            loadData();
            return;
        }

        JDialog dialog = new JDialog(this, LanguageUtils.getText("mm.dialog.edit"), true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 20));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        formPanel.setBackground(Color.WHITE);

        JTextField nameField = new JTextField(member.getName());
        StyleUtils.styleTextField(nameField);

        JTextField phoneField = new JTextField(member.getPhone());
        StyleUtils.styleTextField(phoneField);

        JTextField emailField = new JTextField(member.getEmail());
        StyleUtils.styleTextField(emailField);

        String[] genders = {LanguageUtils.getText("mm.gender.male"), LanguageUtils.getText("mm.gender.female")};
        JComboBox<String> genderCombo = new JComboBox<>(genders);
        genderCombo.setBackground(Color.WHITE);
        String currentGender = "female".equalsIgnoreCase(member.getGender()) ? genders[1] : genders[0];
        genderCombo.setSelectedItem(currentGender);

        JDateChooser birthDateChooser = new JDateChooser();
        birthDateChooser.setDateFormatString("yyyy-MM-dd");
        birthDateChooser.setDate(member.getBirthDate() != null ? member.getBirthDate() : new Date());

        JDateChooser registerDateChooser = new JDateChooser();
        registerDateChooser.setDateFormatString("yyyy-MM-dd");
        registerDateChooser.setDate(member.getRegisterDate());

        formPanel.add(new JLabel(LanguageUtils.getText("mm.col.name") + ":")); formPanel.add(nameField);
        formPanel.add(new JLabel(LanguageUtils.getText("mm.col.phone") + ":")); formPanel.add(phoneField);
        formPanel.add(new JLabel(LanguageUtils.getText("reg.email") + ":")); formPanel.add(emailField);
        formPanel.add(new JLabel(LanguageUtils.getText("mm.col.gender") + ":")); formPanel.add(genderCombo);
        formPanel.add(new JLabel(LanguageUtils.getText("reg.birth") + ":")); formPanel.add(birthDateChooser);
        formPanel.add(new JLabel(LanguageUtils.getText("mm.col.date") + ":")); formPanel.add(registerDateChooser);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        btnPanel.setBackground(Color.WHITE);

        JButton saveBtn = new JButton(LanguageUtils.getText("btn.save"));
        StyleUtils.styleButton(saveBtn, StyleUtils.COLOR_WARNING);

        JButton cancelBtn = new JButton(LanguageUtils.getText("btn.cancel"));
        StyleUtils.styleButton(cancelBtn, StyleUtils.COLOR_INFO);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String genderStr = (String) genderCombo.getSelectedItem();
            String gender = LanguageUtils.getText("mm.gender.male").equals(genderStr) ? "male" : "female";
            Date birthDate = birthDateChooser.getDate();
            Date registerDate = registerDateChooser.getDate();

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || birthDate == null) {
                JOptionPane.showMessageDialog(dialog, LanguageUtils.getText("msg.incomplete"));
                return;
            }

            // 修复点：使用 ServiceResult 而不是 MemberService.ServiceResult
            ServiceResult<Member> resultInfo = memberService.updateMemberInfo(
                    id, name, email, gender, birthDate, registerDate
            );

            if (!resultInfo.isSuccess()) {
                JOptionPane.showMessageDialog(dialog, resultInfo.getMessage(), LanguageUtils.getText("msg.error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!phone.equals(member.getPhone())) {
                memberService.updateMemberPhone(id, phone);
            }

            JOptionPane.showMessageDialog(dialog, "✅ " + LanguageUtils.getText("msg.success"));
            dialog.dispose();
            loadData();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ==================== 删除会员 ====================
    private void deleteMember() {
        int row = memberTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a member first!");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int opt = JOptionPane.showConfirmDialog(this,
                LanguageUtils.getText("btn.delete") + " [" + name + "] ?",
                "Confirm",
                JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            // 修复点：使用 ServiceResult 而不是 MemberService.ServiceResult
            ServiceResult<Void> result = memberService.deleteMember(id);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "✅ " + LanguageUtils.getText("msg.success"));
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "❌ " + result.getMessage(), LanguageUtils.getText("msg.error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}