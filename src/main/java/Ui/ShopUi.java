package Ui;

import entity.Product;
import service.ProductService;
import service.ShopService;
import service.ServiceResult;
import utils.LanguageUtils; // 导入
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopUi extends JFrame {

    private ShopService shopService;
    private ProductService productService;
    private JTable productTable;
    private DefaultTableModel productModel;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    private JTextField searchField;
    private Map<Integer, Integer> shoppingCart = new HashMap<>();
    private Map<Integer, Product> productCache = new HashMap<>();

    public ShopUi() {
        this.shopService = new ShopService();
        this.productService = new ProductService();
        StyleUtils.initGlobalTheme();
        setTitle(LanguageUtils.getText("shop.title"));
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(10, 10));

        initView();
        loadProducts();
        setVisible(true);
    }

    private void initView() {
        // === 左侧 ===
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField(15);
        StyleUtils.styleTextField(searchField);
        searchField.addActionListener(e -> loadProducts());

        JButton searchBtn = new JButton("🔍 " + LanguageUtils.getText("shop.search_btn"));
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> loadProducts());

        JButton showAllBtn = new JButton("🔄 " + LanguageUtils.getText("shop.show_all"));
        StyleUtils.styleButton(showAllBtn, StyleUtils.COLOR_INFO);
        showAllBtn.addActionListener(e -> { searchField.setText(""); loadProducts(); });

        searchPanel.add(new JLabel(LanguageUtils.getText("shop.search_ph") + ":"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(showAllBtn);
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        // 修复：shop.col.name
        String[] pCols = {"ID", LanguageUtils.getText("shop.col.name"), LanguageUtils.getText("shop.col.price"), LanguageUtils.getText("shop.col.stock"), "+"};
        productModel = new DefaultTableModel(pCols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        productTable = new JTable(productModel);
        StyleUtils.styleTable(productTable);
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) addToCart(); }
        });
        JScrollPane pScroll = new JScrollPane(productTable);
        pScroll.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));
        leftPanel.add(pScroll, BorderLayout.CENTER);
        leftPanel.add(new JLabel("💡 Double click to add to cart"), BorderLayout.SOUTH);

        // === 右侧 ===
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(350, 0));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(220, 220, 220)));

        // 语言切换按钮 (右侧顶部)
        JPanel rightTop = new JPanel(new BorderLayout());
        rightTop.setBackground(Color.WHITE);
        JLabel cartTitle = new JLabel("🛍️ " + LanguageUtils.getText("shop.cart_title"), SwingConstants.CENTER);
        cartTitle.setFont(StyleUtils.FONT_TITLE);
        cartTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        rightTop.add(cartTitle, BorderLayout.CENTER);
        JButton langBtn = LanguageUtils.createLanguageButton(this, () -> new ShopUi());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrap.setBackground(Color.WHITE);
        btnWrap.add(langBtn);
        rightTop.add(btnWrap, BorderLayout.NORTH);
        rightPanel.add(rightTop, BorderLayout.NORTH);

        String[] cCols = {LanguageUtils.getText("shop.col.name"), LanguageUtils.getText("shop.col.qty"), "Subtotal"};
        cartModel = new DefaultTableModel(cCols, 0);
        cartTable = new JTable(cartModel);
        StyleUtils.styleTable(cartTable);
        JScrollPane cScroll = new JScrollPane(cartTable);
        cScroll.setBorder(null);
        rightPanel.add(cScroll, BorderLayout.CENTER);

        JPanel checkoutPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        checkoutPanel.setBackground(new Color(245, 250, 255));
        checkoutPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setOpaque(false);
        JButton clearBtn = new JButton(LanguageUtils.getText("shop.clear"));
        StyleUtils.styleButton(clearBtn, StyleUtils.COLOR_INFO);
        clearBtn.addActionListener(e -> clearCart());
        JButton removeBtn = new JButton(LanguageUtils.getText("shop.remove"));
        StyleUtils.styleButton(removeBtn, StyleUtils.COLOR_WARNING);
        removeBtn.addActionListener(e -> removeFromCart());
        btnRow.add(clearBtn); btnRow.add(removeBtn);
        checkoutPanel.add(btnRow);

        totalLabel = new JLabel(LanguageUtils.getText("shop.total") + "¥ 0.00", SwingConstants.CENTER);
        totalLabel.setFont(StyleUtils.FONT_TITLE_BIG);
        totalLabel.setForeground(StyleUtils.COLOR_DANGER);
        checkoutPanel.add(totalLabel);

        JButton checkoutBtn = new JButton("✨ " + LanguageUtils.getText("shop.checkout"));
        StyleUtils.styleButton(checkoutBtn, StyleUtils.COLOR_SUCCESS);
        checkoutBtn.setFont(StyleUtils.FONT_TITLE);
        checkoutBtn.addActionListener(e -> performCheckout());
        checkoutPanel.add(checkoutBtn);
        rightPanel.add(checkoutPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadProducts() {
        productModel.setRowCount(0); productCache.clear();
        String keyword = searchField.getText().trim();
        List<Product> products = keyword.isEmpty() ? productService.getAllProducts() : productService.searchProducts(keyword);
        for (Product p : products) {
            productCache.put(p.getProductId(), p);
            productModel.addRow(new Object[]{p.getProductId(), p.getName(), p.getPrice(), p.getStock(), "➕"});
        }
    }

    private void addToCart() {
        int row = productTable.getSelectedRow();
        if (row == -1) return;
        int pId = (int) productModel.getValueAt(row, 0);
        Product p = productCache.get(pId);
        if (p.getStock() <= 0) { JOptionPane.showMessageDialog(this, "Stock empty!"); return; }
        int currentQty = shoppingCart.getOrDefault(pId, 0);
        if (currentQty >= p.getStock()) { JOptionPane.showMessageDialog(this, "Not enough stock!"); return; }
        shoppingCart.put(pId, currentQty + 1);
        updateCartView();
    }

    private void updateCartView() {
        cartModel.setRowCount(0);
        double total = 0.0;
        for (Map.Entry<Integer, Integer> entry : shoppingCart.entrySet()) {
            Product p = productCache.get(entry.getKey());
            if (p == null) continue;
            int qty = entry.getValue();
            double subtotal = p.getPrice() * qty;
            total += subtotal;
            cartModel.addRow(new Object[]{p.getName(), qty, String.format("%.2f", subtotal)});
        }
        totalLabel.setText(LanguageUtils.getText("shop.total") + "¥ " + String.format("%.2f", total));
    }

    private void clearCart() { shoppingCart.clear(); updateCartView(); }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select item!"); return; }
        String pName = (String) cartModel.getValueAt(row, 0);
        Integer targetId = null;
        for (Map.Entry<Integer, Product> entry : productCache.entrySet()) {
            if (entry.getValue().getName().equals(pName)) { targetId = entry.getKey(); break; }
        }
        if (targetId != null) { shoppingCart.remove(targetId); updateCartView(); }
    }

    private void performCheckout() {
        if (shoppingCart.isEmpty()) { JOptionPane.showMessageDialog(this, "Cart is empty!"); return; }
        String input = JOptionPane.showInputDialog(this, "Enter Member ID (0 for Guest):", "0");
        if (input == null) return;
        try {
            int memberId = Integer.parseInt(input);
            ServiceResult<Void> result = shopService.checkout(memberId, shoppingCart);
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "✅ " + result.getMessage());
                clearCart(); loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "❌ " + result.getMessage());
            }
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Invalid ID"); }
    }
}