package dao;

import entity.MembershipCard;
import entity.MembershipType;
import utils.DBUtil;
import utils.DateUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date; // 导入 SQL Date
import java.util.*;

/**
 * 会员卡数据访问对象
 * 纯粹的数据库操作，不包含业务逻辑（如扣费、订单等）
 */
public class MembershipCardDAO {

    // ==================== 状态常量 ====================
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";
    public static final String STATUS_EXPIRED = "expired";
    public static final String[] VALID_STATUSES = {STATUS_ACTIVE, STATUS_INACTIVE, STATUS_EXPIRED};

    // ==================== 类型常量 ====================
    public static final int TYPE_MONTHLY = 1;
    public static final int TYPE_YEARLY = 2;

    // >>> 新增：价格常量 <<<
    public static final double PRICE_MONTHLY = 200.0;
    public static final double PRICE_YEARLY = 1200.0;

    // ==================== 依赖 ====================
    private MembershipTypeDAO typeDAO;

    // ❌ 删除了 memberDAO, orderDAO, cardDAO 等字段
    // DAO 只专注做自己的表的增删改查，解耦！

    public MembershipCardDAO() {
        this.typeDAO = new MembershipTypeDAO();
    }

    // ==================== 结果集映射 ====================

    private MembershipCard mapResultSetToEntity(ResultSet rs) throws SQLException {
        MembershipCard mc = new MembershipCard();
        mc.setCardId(rs.getInt("card_id"));
        mc.setMemberId(rs.getInt("member_id"));
        mc.setTypeId(rs.getInt("type_id"));
        mc.setStartDate(rs.getDate("start_date"));
        mc.setEndDate(rs.getDate("end_date"));
        mc.setCardStatus(rs.getString("card_status"));
        return mc;
    }

    private MembershipCard mapResultSetToEntityWithType(ResultSet rs) throws SQLException {
        MembershipCard mc = mapResultSetToEntity(rs);
        MembershipType type = typeDAO.getTypeById(mc.getTypeId());
        mc.setMembershipType(type);
        return mc;
    }

    // ==================== 核心查询 ====================

    public MembershipCard getById(int cardId) {
        String sql = "SELECT * FROM membership_card WHERE card_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToEntityWithType(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<MembershipCard> getByMemberId(int memberId) {
        List<MembershipCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM membership_card WHERE member_id = ? ORDER BY card_id DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) cards.add(mapResultSetToEntityWithType(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return cards;
    }

    /**
     * 获取会员当前有效的会员卡
     */
    public MembershipCard getActiveMembershipCard(int memberId) {
        String sql = "SELECT * FROM membership_card WHERE member_id = ? AND card_status = ? AND end_date >= CURDATE() ORDER BY end_date DESC LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            pstmt.setString(2, STATUS_ACTIVE);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToEntityWithType(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean hasMemberValidCard(int memberId) {
        return getActiveMembershipCard(memberId) != null;
    }

    // ==================== 增改操作 ====================

    public boolean addMembershipCard(MembershipCard card) {
        // 注意：这里去掉了 memberDAO 的检查，防止循环依赖。
        // 应该在 Service 层保证 memberId 是有效的。

        String sql = "INSERT INTO membership_card (member_id, type_id, start_date, end_date, card_status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, card.getMemberId());
            pstmt.setInt(2, card.getTypeId());
            pstmt.setDate(3, DateUtils.toSqlDate(card.getStartDate()));
            pstmt.setDate(4, DateUtils.toSqlDate(card.getEndDate()));
            pstmt.setString(5, card.getCardStatus());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) card.setCardId(rs.getInt(1));
                }
            }
            return affectedRows > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ==================== 关键：续费底层方法 ====================

    /**
     * 延长会员卡有效期 (这是 DAO 应该做的事：只改日期)
     */
    public boolean extendValidity(int cardId, int daysToAdd) {
        String sql = "UPDATE membership_card SET end_date = DATE_ADD(end_date, INTERVAL ? DAY) WHERE card_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, daysToAdd);
            pstmt.setInt(2, cardId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== 便捷创建方法 ====================

    public boolean createMonthlyCard(int memberId) {
        MembershipCard card = new MembershipCard();
        card.setMemberId(memberId);
        card.setTypeId(TYPE_MONTHLY);
        card.setStartDate(DateUtils.now());
        card.setEndDate(DateUtils.getMonthlyCardEndDate());
        card.setCardStatus(STATUS_ACTIVE);
        return addMembershipCard(card);
    }

    public boolean createYearlyCard(int memberId) {
        MembershipCard card = new MembershipCard();
        card.setMemberId(memberId);
        card.setTypeId(TYPE_YEARLY);
        card.setStartDate(DateUtils.now());
        card.setEndDate(DateUtils.getYearlyCardEndDate());
        card.setCardStatus(STATUS_ACTIVE);
        return addMembershipCard(card);
    }

    // ... 其他 getter/setter 或者校验方法保留即可 ...
    // ... 但是请删除那个 renewMembership 方法 ...
}