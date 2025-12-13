package Ui;

import entity.Booking;
import entity.Member;
import service.BookingService;
import utils.LanguageUtils; // 引入
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyBookingUi extends JFrame {

    private Member member;
    private BookingService bookingService;
    private JTable bookingTable;
    private DefaultTableModel tableModel;

    public MyBookingUi(Member member) {
        this.member = member;
        this.bookingService = new BookingService();
        StyleUtils.initGlobalTheme();
        setTitle("📋 " + LanguageUtils.getText("mybook.title"));
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(15, 15));
        initView();
        loadMyBookings();
        setVisible(true);
    }

    private void initView() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLbl = new JLabel("📅 " + LanguageUtils.getText("mybook.title"));
        titleLbl.setFont(StyleUtils.FONT_TITLE);
        titleLbl.setForeground(StyleUtils.COLOR_TEXT_MAIN);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton refreshBtn = new JButton("🔄 " + LanguageUtils.getText("btn.refresh"));
        StyleUtils.styleButton(refreshBtn, StyleUtils.COLOR_PRIMARY);
        refreshBtn.addActionListener(e -> loadMyBookings());
        btnPanel.add(refreshBtn);

        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new MyBookingUi(member));
        btnPanel.add(langBtn);

        topPanel.add(titleLbl, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        String[] cols = {"ID", LanguageUtils.getText("cm.col.course"), LanguageUtils.getText("cm.col.time"), LanguageUtils.getText("cm.col.trainer"), LanguageUtils.getText("mybook.col.status"), "Action"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        bookingTable = new JTable(tableModel);
        StyleUtils.styleTable(bookingTable);
        bookingTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) cancelBooking(); }
        });
        JScrollPane scroll = new JScrollPane(bookingTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);
        centerPanel.add(scroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(StyleUtils.COLOR_BG);
        JLabel tipLbl = new JLabel("💡 " + LanguageUtils.getText("mybook.tip"));
        tipLbl.setFont(StyleUtils.FONT_NORMAL);
        tipLbl.setForeground(StyleUtils.COLOR_INFO);
        bottomPanel.add(tipLbl);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadMyBookings() {
        tableModel.setRowCount(0);
        List<Booking> list = bookingService.getBookingsByMember(member.getId());
        for (Booking b : list) {
            service.BookingService.BookingDetail detail = bookingService.getBookingDetail(b.getBookingId());
            String courseName = (detail != null) ? detail.getCourseName() : "?";
            String trainer = (detail != null) ? detail.getTrainerName() : "-";
            String time = (detail != null && detail.getCourse() != null) ? "" + detail.getCourse().getCourseTime() : "-";
            tableModel.addRow(new Object[]{b.getBookingId(), courseName, time, trainer, b.getBookingStatus(), "Cancel"});
        }
    }

    private void cancelBooking() {
        int row = bookingTable.getSelectedRow();
        if (row == -1) return;
        int bookingId = (int) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, LanguageUtils.getText("btn.delete") + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            bookingService.memberCancelBooking(member.getId(), bookingId);
            loadMyBookings();
        }
    }
}