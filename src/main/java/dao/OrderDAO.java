package dao;

import entity.Order;
import utils.DBUtil;
import utils.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单数据访问对象
 * 对应数据库 order 表
 */
public class OrderDAO {

    // ========== 订单类型常量 (关键修复：补全类型) ==========
    public static final String TYPE_MEMBERSHIP = "membership";
    public static final String TYPE_PRODUCT = "product";
    public static final String TYPE_COURSE = "course";
    // >>> 新增 <<<
    public static final String TYPE_RECHARGE = "recharge";
    public static final String TYPE_RENEWAL = "renewal";

    // ========== 支付状态常量 ==========
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PAID = "paid";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_REFUNDED = "refunded";

    // 有效的订单类型 (关键修复：加入 RECHARGE 和 RENEWAL)
    public static final String[] VALID_TYPES = {
            TYPE_MEMBERSHIP, TYPE_PRODUCT, TYPE_COURSE, TYPE_RECHARGE, TYPE_RENEWAL
    };

    // 有效的支付状态
    public static final String[] VALID_STATUSES = {STATUS_PENDING, STATUS_PAID, STATUS_CANCELLED, STATUS_REFUNDED};

    public OrderDAO() {
    }

    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setMemberId(rs.getInt("member_id"));
        order.setOrderType(rs.getString("order_type"));
        order.setAmount(rs.getDouble("amount"));
        order.setOrderTime(rs.getTimestamp("order_time"));
        order.setPaymentStatus(rs.getString("payment_status"));
        return order;
    }

    private boolean isValidType(String type) {
        if (type == null) return false;
        for (String validType : VALID_TYPES) {
            if (validType.equals(type)) return true;
        }
        return false;
    }

    private boolean isValidStatus(String status) {
        if (status == null) return false;
        for (String validStatus : VALID_STATUSES) {
            if (validStatus.equals(status)) return true;
        }
        return false;
    }

    // ========== 基础 CRUD ==========

    public boolean addOrder(Order order) {
        if (order == null) return false;
        // 这里就是之前报错的地方，现在补全了 VALID_TYPES 就不会报错了
        if (!isValidType(order.getOrderType())) {
            System.err.println("添加失败：无效的订单类型 (type=" + order.getOrderType() + ")");
            return false;
        }

        String sql = "INSERT INTO `order` (member_id, order_type, amount, order_time, payment_status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            // 处理散客 (ID<=0 存 NULL)
            if (order.getMemberId() <= 0) {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(1, order.getMemberId());
            }
            pstmt.setString(2, order.getOrderType());
            pstmt.setDouble(3, order.getAmount());
            pstmt.setTimestamp(4, order.getOrderTime() != null ?
                    DateUtils.toSqlTimestamp(order.getOrderTime()) : DateUtils.nowTimestamp());
            pstmt.setString(5, order.getPaymentStatus() != null ?
                    order.getPaymentStatus() : STATUS_PENDING);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) order.setOrderId(rs.getInt(1));
                }
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM `order` WHERE order_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return extractOrderFromResultSet(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `order` ORDER BY order_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) orders.add(extractOrderFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    public boolean updateOrder(Order order) {
        String sql = "UPDATE `order` SET member_id = ?, order_type = ?, amount = ?, order_time = ?, payment_status = ? WHERE order_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, order.getMemberId());
            pstmt.setString(2, order.getOrderType());
            pstmt.setDouble(3, order.getAmount());
            pstmt.setTimestamp(4, DateUtils.toSqlTimestamp(order.getOrderTime()));
            pstmt.setString(5, order.getPaymentStatus());
            pstmt.setInt(6, order.getOrderId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean deleteOrder(int orderId) {
        String sql = "DELETE FROM `order` WHERE order_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // ========== 查询功能 ==========
    public List<Order> getOrdersByMemberId(int memberId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `order` WHERE member_id = ? ORDER BY order_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) orders.add(extractOrderFromResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    // ========== 统计功能 ==========
    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS revenue FROM `order` WHERE payment_status = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, STATUS_PAID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("revenue");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    // ... (其他查询方法如 getOrdersByType 等保留原样即可，不影响核心功能)
}