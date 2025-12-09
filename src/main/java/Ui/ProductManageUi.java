package Ui;

import entity.Product;
import service.ProductService;

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
        setTitle("商品/库存管理");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); // 使用 BorderLayout

        initView();
        loadProductsToTable();

        setVisible(true);
    }

    private void initView() {
        // --- 顶部搜索和操作区域 ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        add(topPanel, BorderLayout.NORTH);

        searchField = new JTextField(15);
        topPanel.add(new JLabel("搜索名称:"));
        topPanel.add(searchField);

        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> searchProduct());
        topPanel.add(searchBtn);

        JButton refreshBtn = new JButton("刷新列表");
        refreshBtn.addActionListener(e -> loadProductsToTable());
        topPanel.add(refreshBtn);

        topPanel.add(new JSeparator(SwingConstants.VERTICAL));

        // --- CRUD 操作按钮 ---
        JButton addBtn = new JButton("新增商品");
        addBtn.addActionListener(e -> openAddEditDialog(null));
        topPanel.add(addBtn);

        JButton editBtn = new JButton("修改信息");
        editBtn.addActionListener(e -> editProduct());
        topPanel.add(editBtn);

        JButton deleteBtn = new JButton("删除商品");
        deleteBtn.addActionListener(e -> deleteProduct());
        topPanel.add(deleteBtn);

        // --- 表格主体 ---
        String[] columns = {"ID", "名称", "单价 (¥)", "当前库存"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setRowHeight(25);
        add(new JScrollPane(productTable), BorderLayout.CENTER);
    }

    // ==================== 数据加载与操作 ====================

    /**
     * 从数据库加载数据并填充表格
     */
    private void loadProductsToTable() {
        tableModel.setRowCount(0);
        List<Product> products = productService.getAllProducts();

        for (Product p : products) {
            tableModel.addRow(new Object[]{
                    p.getProductId(),
                    p.getName(),
                    p.getPrice(),
                    p.getStock()
            });
        }
        setTitle(String.format("商品/库存管理 (共 %d 种商品)", products.size()));
    }

    /**
     * 搜索商品
     */
    private void searchProduct() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadProductsToTable();
            return;
        }

        tableModel.setRowCount(0);
        List<Product> products = productService.searchProducts(keyword);

        for (Product p : products) {
            tableModel.addRow(new Object[]{
                    p.getProductId(),
                    p.getName(),
                    p.getPrice(),
                    p.getStock()
            });
        }
    }

    /**
     * 删除产品
     */
    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的商品！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除商品 [" + name + "] 吗？\n删除后该商品将无法售卖，相关订单记录将保留。",
                "确认删除", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (productService.deleteProduct(productId)) {
                JOptionPane.showMessageDialog(this, "✅ 删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadProductsToTable(); // 刷新列表
            } else {
                JOptionPane.showMessageDialog(this, "❌ 删除失败！可能存在关联数据。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 编辑产品信息，打开编辑对话框
     */
    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要修改的商品！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取选中行的 Product 对象
        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        double price = (double) tableModel.getValueAt(selectedRow, 2);
        int stock = (int) tableModel.getValueAt(selectedRow, 3);

        Product p = new Product();
        p.setProductId(productId);
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);

        openAddEditDialog(p); // 打开编辑对话框
    }


    // ==================== 增改对话框 ====================

    /**
     * 打开新增或编辑商品的对话框
     */
    private void openAddEditDialog(Product product) {
        boolean isEdit = (product != null);
        JDialog dialog = new JDialog(this, isEdit ? "编辑商品" : "新增商品", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        // --- 组件 ---
        JTextField nameField = new JTextField(isEdit ? product.getName() : "");
        JTextField priceField = new JTextField(isEdit ? String.valueOf(product.getPrice()) : "");
        JTextField stockField = new JTextField(isEdit ? String.valueOf(product.getStock()) : "");

        // --- 布局 ---
        if (isEdit) {
            dialog.add(new JLabel("商品ID (不可改):"));
            dialog.add(new JLabel(String.valueOf(product.getProductId())));
        } else {
            dialog.add(new JLabel()); // 占位
            dialog.add(new JLabel());
        }

        dialog.add(new JLabel("商品名称:"));
        dialog.add(nameField);

        dialog.add(new JLabel("单价 (¥):"));
        dialog.add(priceField);

        dialog.add(new JLabel("库存数量:"));
        dialog.add(stockField);

        JButton saveBtn = new JButton(isEdit ? "保存修改" : "添加商品");
        saveBtn.addActionListener(e -> {
            // 验证并保存
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());

                if (name.isEmpty() || price <= 0 || stock < 0) {
                    JOptionPane.showMessageDialog(dialog, "请检查输入项：名称不能为空，价格必须大于0，库存不能小于0。", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Product newProduct = product;
                if (!isEdit) {
                    newProduct = new Product();
                }

                newProduct.setName(name);
                newProduct.setPrice(price);
                newProduct.setStock(stock);

                boolean success;
                if (isEdit) {
                    success = productService.updateProduct(newProduct);
                } else {
                    success = productService.addProduct(newProduct);
                }

                if (success) {
                    JOptionPane.showMessageDialog(dialog, isEdit ? "修改成功！" : "添加成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose(); // 关闭弹窗
                    loadProductsToTable(); // 刷新主界面
                } else {
                    JOptionPane.showMessageDialog(dialog, isEdit ? "修改失败！" : "添加失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "价格和库存必须是有效数字！", "输入错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(saveBtn);
        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.add(cancelBtn);

        dialog.setVisible(true);
    }
}