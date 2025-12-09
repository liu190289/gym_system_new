package Ui;

import entity.Member;
import entity.MembershipCard;
import service.MemberService;
import dao.MembershipCardDAO;

import javax.swing.*;
import java.awt.*;

public class RenewUi extends JDialog {

    private MemberService memberService;
    private MembershipCardDAO cardDAO;
    private Member currentMember;
    private MembershipCard currentCard;
    private boolean isStaffOperation; // 关键标志位：是否为员工操作

    // 组件
    private JComboBox<String> daysComboBox; // 会员用的固定选项
    private JTextField daysField;           // 员工用的自由输入
    private JTextField priceField;
    private JRadioButton balanceRadio;
    private JRadioButton cashRadio;
    private JLabel balanceTipLabel;

    /**
     * @param owner 父窗口
     * @param member 要续费的会员
     * @param isStaffOperation true=员工/管理员操作, false=会员自己操作
     */
    public RenewUi(Frame owner, Member member, boolean isStaffOperation) {
        super(owner, isStaffOperation ? "办理续费 (员工通道)" : "自助续费", true);
        this.currentMember = member;
        this.isStaffOperation = isStaffOperation;

        this.memberService = new MemberService();
        this.cardDAO = new MembershipCardDAO();
        this.currentCard = cardDAO.getActiveMembershipCard(member.getId());

        setSize(450, 450);
        setLocationRelativeTo(owner);
        setLayout(null);

        if (currentCard == null) {
            String msg = isStaffOperation ? "该会员当前无有效卡，请先进行【开卡】操作。" : "您当前没有有效会员卡，请前往前台办理开卡！";
            JOptionPane.showMessageDialog(owner, msg);
            dispose(); // 没卡不能续费，直接关
            return;
        }

        initView();
        setVisible(true);
    }

    private void initView() {
        int x = 40, w = 350, h = 30;
        int y = 20;

        // 1. 标题信息
        JLabel title = new JLabel("为 [" + currentMember.getName() + "] 办理续费");
        title.setFont(new Font("微软雅黑", Font.BOLD, 16));
        title.setForeground(new Color(0, 102, 204));
        title.setBounds(x, y, w, h);
        add(title);

        y += 40;
        JLabel infoLabel = new JLabel("当前有效期至：" + currentCard.getEndDate());
        infoLabel.setBounds(x, y, w, h);
        add(infoLabel);

        // 2. 续费方案 (区分权限)
        y += 50;
        add(createLabel("续费时长:", x, y));

        if (isStaffOperation) {
            // --- 员工模式：自由输入天数 ---
            daysField = new JTextField("30");
            daysField.setBounds(x + 80, y, 100, h);
            add(daysField);
            JLabel dLabel = new JLabel("天");
            dLabel.setBounds(x + 190, y, 30, h);
            add(dLabel);
        } else {
            // --- 会员模式：只能选 30 或 365 ---
            String[] options = {"月卡续费 (30天)", "年卡续费 (365天)"};
            daysComboBox = new JComboBox<>(options);
            daysComboBox.setBounds(x + 80, y, 200, h);
            // 监听选择改变价格 (这里简单硬编码演示，实际应查数据库价格)
            daysComboBox.addActionListener(e -> updatePriceForMember());
            add(daysComboBox);
        }

        // 3. 价格显示
        y += 50;
        add(createLabel("应付金额:", x, y));
        priceField = new JTextField();
        priceField.setBounds(x + 80, y, 100, h);
        priceField.setFont(new Font("Arial", Font.BOLD, 14));
        if (!isStaffOperation) {
            priceField.setEditable(false); // 会员不能自己改价格
            updatePriceForMember(); // 初始化价格
        } else {
            priceField.setText("200"); // 员工默认值
        }
        add(priceField);
        JLabel yuanLabel = new JLabel("元");
        yuanLabel.setBounds(x + 190, y, 30, h);
        add(yuanLabel);

        // 4. 支付方式 (核心权限控制)
        y += 50;
        add(createLabel("支付方式:", x, y));

        balanceRadio = new JRadioButton("余额支付");
        balanceRadio.setBounds(x + 80, y, 100, h);
        balanceRadio.setSelected(true);
        add(balanceRadio);

        cashRadio = new JRadioButton("现金/其它");
        cashRadio.setBounds(x + 180, y, 100, h);

        ButtonGroup group = new ButtonGroup();
        group.add(balanceRadio);
        group.add(cashRadio);

        if (!isStaffOperation) {
            // 会员模式：禁用现金选项，强制余额
            cashRadio.setEnabled(false);
            cashRadio.setVisible(false); // 或者直接隐藏
            balanceRadio.setText("余额支付 (唯一)");
        } else {
            add(cashRadio); // 员工模式：显示现金选项
        }

        // 余额提示
        y += 30;
        balanceTipLabel = new JLabel("当前账户余额: ¥" + currentMember.getBalance());
        balanceTipLabel.setForeground(Color.GRAY);
        balanceTipLabel.setBounds(x + 80, y, 250, 20);
        add(balanceTipLabel);

        // 5. 确认按钮
        y += 50;
        JButton confirmBtn = new JButton("确认续费");
        confirmBtn.setBackground(new Color(34, 139, 34));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        confirmBtn.setBounds(x, y, 350, 45);
        confirmBtn.addActionListener(e -> performRenew());
        add(confirmBtn);
    }

    private JLabel createLabel(String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("微软雅黑", Font.BOLD, 14));
        l.setBounds(x, y, 80, 30);
        return l;
    }

    // 会员模式下自动计算价格
    private void updatePriceForMember() {
        int idx = daysComboBox.getSelectedIndex();
        if (idx == 0) priceField.setText("200.0");  // 月卡价格
        else priceField.setText("1200.0"); // 年卡价格
    }

    private void performRenew() {
        try {
            int days;
            if (isStaffOperation) {
                days = Integer.parseInt(daysField.getText().trim());
            } else {
                // 根据下拉框判断天数
                days = (daysComboBox.getSelectedIndex() == 0) ? 30 : 365;
            }

            double price = Double.parseDouble(priceField.getText().trim());

            // 会员只能余额支付，员工看选项
            boolean useBalance = isStaffOperation ? balanceRadio.isSelected() : true;

            // 确认弹窗
            int opt = JOptionPane.showConfirmDialog(this,
                    "确认续费 " + days + " 天？\n金额：¥" + price, "确认", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;

            // 调用 Service
            MemberService.ServiceResult<Void> result = memberService.renewMembership(currentMember.getId(), days, price, useBalance);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "✅ " + result.getMessage());
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ " + result.getMessage());
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "输入格式错误！");
        }
    }
}