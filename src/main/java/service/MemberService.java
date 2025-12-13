package service;

import dao.MemberDAO;
import dao.MembershipCardDAO;
import dao.BookingDAO;
import dao.CheckInDAO;
import dao.OrderDAO;
import entity.Member;
import entity.MembershipCard;
import utils.DateUtils;
import java.util.Date;
import java.util.List;

public class MemberService {

    private MemberDAO memberDAO;
    private MembershipCardDAO cardDAO;
    private BookingDAO bookingDAO;
    private CheckInDAO checkInDAO;
    private OrderDAO orderDAO;

    public MemberService() {
        this.memberDAO = new MemberDAO();
        this.cardDAO = new MembershipCardDAO();
        this.bookingDAO = new BookingDAO();
        this.checkInDAO = new CheckInDAO();
        this.orderDAO = new OrderDAO();
    }

    // ==================== 核心业务：注册 ====================
    public ServiceResult<Member> register(String name, String phone, String email, String gender, Date birthDate) {
        if (name == null || name.trim().isEmpty()) return ServiceResult.failure("姓名不能为空");
        if (memberDAO.isPhoneExists(phone)) return ServiceResult.failure("手机号已存在");

        Member member = new Member();
        member.setName(name.trim());
        member.setPhone(phone);
        member.setEmail(email);
        member.setGender(gender);
        member.setBirthDate(birthDate);
        member.setRegisterDate(DateUtils.now());
        member.setStatus(MemberDAO.STATUS_ACTIVE);

        if (memberDAO.addMember(member)) {
            return ServiceResult.success("注册成功", member);
        } else {
            return ServiceResult.failure("注册失败：数据库错误");
        }
    }

    // ==================== 核心业务：开卡 ====================
    public ServiceResult<Void> buyCard(int memberId, int cardType, double price) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) return ServiceResult.failure("会员不存在");
        if (cardDAO.hasMemberValidCard(memberId)) return ServiceResult.failure("已有有效会员卡，请使用续费！");

        boolean success = false;
        String typeName = "";

        if (cardType == MembershipCardDAO.TYPE_MONTHLY) {
            success = cardDAO.createMonthlyCard(memberId);
            typeName = "月卡";
        } else if (cardType == MembershipCardDAO.TYPE_YEARLY) {
            success = cardDAO.createYearlyCard(memberId);
            typeName = "年卡";
        } else {
            return ServiceResult.failure("无效卡类型");
        }

        if (success) {
            try {
                entity.Order order = new entity.Order();
                order.setMemberId(memberId);
                order.setOrderType(OrderDAO.TYPE_MEMBERSHIP);
                order.setAmount(price);
                order.setPaymentStatus(OrderDAO.STATUS_PAID);
                order.setOrderTime(utils.DateUtils.now());
                orderDAO.addOrder(order);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ServiceResult.success("开卡成功！已开通 " + typeName);
        } else {
            return ServiceResult.failure("开卡失败：数据库错误");
        }
    }

    public ServiceResult<Void> buyCard(int memberId, int cardType) {
        double price = 0.0;
        if (cardType == MembershipCardDAO.TYPE_MONTHLY) {
            price = MembershipCardDAO.PRICE_MONTHLY;
        } else if (cardType == MembershipCardDAO.TYPE_YEARLY) {
            price = MembershipCardDAO.PRICE_YEARLY;
        }
        return buyCard(memberId, cardType, price);
    }

    // ==================== 核心业务：续费 ====================
    public ServiceResult<Void> renewMembership(int memberId, int days, double price, boolean useBalance) {
        if (days <= 0 || price < 0) return ServiceResult.failure("参数错误");
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) return ServiceResult.failure("会员不存在");
        MembershipCard activeCard = cardDAO.getActiveMembershipCard(memberId);
        if (activeCard == null) return ServiceResult.failure("无有效卡，请先开卡");

        if (useBalance) {
            if (member.getBalance() < price) return ServiceResult.failure("余额不足: " + member.getBalance());
            if (!memberDAO.updateBalance(memberId, member.getBalance() - price)) return ServiceResult.failure("扣款失败");
        }

        if (cardDAO.extendValidity(activeCard.getCardId(), days)) {
            try {
                entity.Order order = new entity.Order();
                order.setMemberId(memberId);
                order.setOrderType(OrderDAO.TYPE_RENEWAL);
                order.setAmount(price);
                order.setOrderTime(utils.DateUtils.now());
                order.setPaymentStatus(OrderDAO.STATUS_PAID);
                orderDAO.addOrder(order);
            } catch (Exception e) { e.printStackTrace(); }
            return ServiceResult.success("续费成功");
        } else {
            if (useBalance) memberDAO.updateBalance(memberId, member.getBalance());
            return ServiceResult.failure("续费失败");
        }
    }

    // ==================== 查询方法 (补全缺失部分) ====================

    /**
     * 获取所有会员列表
     */
    public List<Member> getAllMembers() {
        return memberDAO.getAllMembers();
    }

    /**
     * 根据ID获取会员
     */
    public Member getMemberById(int id) {
        return memberDAO.getMemberById(id);
    }

    /**
     * 综合搜索 (支持姓名或手机号)
     */
    public List<Member> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllMembers();
        }
        // 如果是纯数字，尝试按手机号搜，否则按姓名搜
        if (keyword.matches("\\d+")) {
            Member m = memberDAO.getMemberByPhone(keyword);
            return m != null ? java.util.Collections.singletonList(m) : new java.util.ArrayList<>();
        }
        return memberDAO.searchMembersByName(keyword);
    }

    // ==================== 更新与删除 ====================

    /**
     * 更新会员基本信息
     */
    public ServiceResult<Member> updateMemberInfo(int memberId, String name, String email, String gender, Date birthDate, Date registerDate) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) return ServiceResult.failure("会员不存在");

        member.setName(name);
        member.setEmail(email);
        member.setGender(gender);
        member.setBirthDate(birthDate);
        if (registerDate != null) {
            member.setRegisterDate(registerDate);
        }

        if (memberDAO.updateMember(member)) {
            return ServiceResult.success("更新成功", member);
        } else {
            return ServiceResult.failure("数据库更新失败");
        }
    }

    /**
     * 更新手机号
     */
    public ServiceResult<Member> updateMemberPhone(int memberId, String newPhone) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) return ServiceResult.failure("会员不存在");

        if (memberDAO.isPhoneExists(newPhone)) {
            // 简单的查重逻辑：如果手机号存在且不是本人
            Member exist = memberDAO.getMemberByPhone(newPhone);
            if (exist != null && exist.getId() != memberId) {
                return ServiceResult.failure("手机号已被占用");
            }
        }

        member.setPhone(newPhone);
        return memberDAO.updateMember(member) ? ServiceResult.success("手机号更新成功", member) : ServiceResult.failure("更新失败");
    }

    /**
     * 删除会员
     */
    public ServiceResult<Void> deleteMember(int memberId) {
        // 这里可以加更严格的检查，比如有余额不能删等
        if (memberDAO.deleteMember(memberId)) {
            return ServiceResult.success("删除成功");
        } else {
            return ServiceResult.failure("删除失败，可能存在关联数据");
        }
    }
}