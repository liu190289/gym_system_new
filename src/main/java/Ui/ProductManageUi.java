package Ui;

import entity.Product;
import service.ProductService;
import utils.LanguageUtils; // 导入
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductManageUi extends JFrame {

    private ProductService productService;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public ProductManageUi() {
        this.productService = new ProductService();
        StyleUtils.initGlobalTheme();
        setTitle(LanguageUtils.getText("pm.title"));
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(10, 10));
        initView();
        loadProductsToTable();
        setVisible(true);
    }

    private void initView() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        add(topPanel, BorderLayout.NORTH);

        // 修复：shop.col.name
        topPanel.add(new JLabel("📦 " + LanguageUtils.getText("shop.col.name") + ":"));
        searchField = new JTextField(15);
        StyleUtils.styleTextField(searchField);
        topPanel.add(searchField);

        JButton searchBtn = new JButton(LanguageUtils.getText("btn.search"));
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> searchProduct());
        topPanel.add(searchBtn);

        JButton refreshBtn = new JButton("🔄 " + LanguageUtils.getText("btn.refresh"));
        StyleUtils.styleButton(refreshBtn, StyleUtils.COLOR_INFO);
        refreshBtn.addActionListener(e -> loadProductsToTable());
        topPanel.add(refreshBtn);

        topPanel.add(new JSeparator(SwingConstants.VERTICAL));

        // 修复：pm.add
        JButton addBtn = new JButton("➕ " + LanguageUtils.getText("pm.add"));
        StyleUtils.styleButton(addBtn, StyleUtils.COLOR_SUCCESS);
        addBtn.addActionListener(e -> openAddEditDialog(null));
        topPanel.add(addBtn);

        // 修复：pm.edit
        JButton editBtn = new JButton("✏️ " + LanguageUtils.getText("pm.edit"));
        StyleUtils.styleButton(editBtn, StyleUtils.COLOR_WARNING);
        editBtn.addActionListener(e -> editProduct());
        topPanel.add(editBtn);

        JButton delBtn = new JButton("🗑️ " + LanguageUtils.getText("pm.del"));
        StyleUtils.styleButton(delBtn, StyleUtils.COLOR_DANGER);
        delBtn.addActionListener(e -> deleteProduct());
        topPanel.add(delBtn);

        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new ProductManageUi());
        topPanel.add(langBtn);

        // 修复表头
        String[] columns = {"ID", LanguageUtils.getText("shop.col.name"), LanguageUtils.getText("shop.col.price"), LanguageUtils.getText("shop.col.stock")};
        tableModel = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        productTable = new JTable(tableModel);
        StyleUtils.styleTable(productTable);
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadProductsToTable() {
        tableModel.setRowCount(0);
        List<Product> list = productService.getAllProducts();
        for (Product p : list) tableModel.addRow(new Object[]{p.getProductId(), p.getName(), p.getPrice(), p.getStock()});
    }

    private void searchProduct() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadProductsToTable(); return; }
        tableModel.setRowCount(0);
        List<Product> list = productService.searchProducts(kw);
        for (Product p : list) tableModel.addRow(new Object[]{p.getProductId(), p.getName(), p.getPrice(), p.getStock()});
    }

    private void editProduct() {
        int row = productTable.getSelectedRow();
        if (row == -1) return;
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        double price = (double) tableModel.getValueAt(row, 2);
        int stock = (int) tableModel.getValueAt(row, 3);
        Product p = new Product(); p.setProductId(id); p.setName(name); p.setPrice(price); p.setStock(stock);
        openAddEditDialog(p);
    }

    private void deleteProduct() {
        int row = productTable.getSelectedRow();
        if (row == -1) return;
        int id = (int) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, LanguageUtils.getText("btn.delete") + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            productService.deleteProduct(id);
            loadProductsToTable();
        }
    }

    private void openAddEditDialog(Product product) {
        boolean isEdit = (product != null);
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField nameF = new JTextField(isEdit ? product.getName() : "");
        JTextField priceF = new JTextField(isEdit ? String.valueOf(product.getPrice()) : "");
        JTextField stockF = new JTextField(isEdit ? String.valueOf(product.getStock()) : "");
        panel.add(new JLabel(LanguageUtils.getText("shop.col.name") + ":")); panel.add(nameF);
        panel.add(new JLabel(LanguageUtils.getText("shop.col.price") + ":")); panel.add(priceF);
        panel.add(new JLabel(LanguageUtils.getText("shop.col.stock") + ":")); panel.add(stockF);

        String title = isEdit ? LanguageUtils.getText("pm.edit") : LanguageUtils.getText("pm.add");
        if (JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String name = nameF.getText().trim();
                double price = Double.parseDouble(priceF.getText().trim());
                int stock = Integer.parseInt(stockF.getText().trim());
                Product newP = isEdit ? product : new Product();
                newP.setName(name); newP.setPrice(price); newP.setStock(stock);
                if (isEdit) productService.updateProduct(newP); else productService.addProduct(newP);
                loadProductsToTable();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid Input"); }
        }
    }
}