package service;

import dao.ProductDAO;
import entity.Product;
import java.util.List;

/**
 * 产品业务服务层
 */
public class ProductService {
    private ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    public boolean addProduct(Product product) {
        return productDAO.addProduct(product);
    }

    public boolean updateProduct(Product product) {
        return productDAO.updateProduct(product);
    }

    public boolean deleteProduct(int productId) {
        // 通常需要检查是否有未完成的订单关联此产品，但这里简化为直接删除
        return productDAO.deleteProduct(productId);
    }

    public List<Product> searchProducts(String keyword) {
        return productDAO.searchProductsByName(keyword);
    }
}