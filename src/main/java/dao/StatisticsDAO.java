package dao;

import utils.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsDAO {

    /**
     * 获取总收入 (所有已支付订单的金额总和)
     */
    public double getTotalRevenue() {
        // IFNULL/COALESCE 防止没有订单时返回 NULL 导致报错
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM `order` WHERE payment_status LIKE 'paid%'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * 获取总会员数
     */
    public int getTotalMembers() {
        String sql = "SELECT COUNT(*) FROM member";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取今日订单数 (监控业务活跃度)
     */
    public int getTodayOrderCount() {
        // CURDATE() 是 MySQL 获取当前日期的函数
        String sql = "SELECT COUNT(*) FROM `order` WHERE DATE(order_time) = CURDATE()";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取库存紧张的商品数量 (假设库存 < 10 算紧张)
     */
    public int getLowStockProductCount() {
        String sql = "SELECT COUNT(*) FROM product WHERE stock < 10";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取最近的 50 条订单记录 (用于列表展示)
     * 包含：订单ID, 会员名(联表), 类型, 金额, 时间
     */
    public List<Map<String, Object>> getRecentOrders() {
        List<Map<String, Object>> list = new ArrayList<>();
        // 联表查询：order 表左连接 member 表，以便拿到会员名字（如果是散客则为NULL）
        String sql = "SELECT o.order_id, m.name AS member_name, o.order_type, o.amount, o.order_time " +
                "FROM `order` o " +
                "LEFT JOIN member m ON o.member_id = m.member_id " +
                "ORDER BY o.order_time DESC LIMIT 50";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("order_id"));
                // 如果 member_name 是 null，说明是散客
                String name = rs.getString("member_name");
                map.put("name", name == null ? "散客" : name);
                map.put("type", rs.getString("order_type"));
                map.put("amount", rs.getDouble("amount"));
                map.put("time", rs.getTimestamp("order_time"));
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}