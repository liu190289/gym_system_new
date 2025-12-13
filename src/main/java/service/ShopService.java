package service;

import dao.MemberDAO;
import dao.OrderDAO;
import dao.OrderProductDAO;
import dao.ProductDAO;
import entity.Member;
import entity.Order;
import entity.OrderProduct;
import entity.Product;
import utils.DateUtils;

import java.math.BigDecimal;
import java.util.Map;

public class ShopService {

    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private OrderProductDAO orderProductDAO;
    private MemberDAO memberDAO;

    public ShopService() {
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
        this.orderProductDAO = new OrderProductDAO();
        this.memberDAO = new MemberDAO();
    }

    public java.util.List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    /**
     * 购物结算 (Checkout) - 保持不变，逻辑是正确的
     */
    public ServiceResult<Void> checkout(int memberId, Map<Integer, Integer> cart) {
        if (cart == null || cart.isEmpty()) return ServiceResult.failure("购物车为空");
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Product p = productDAO.getProductById(entry.getKey());
            if (p == null || p.getStock() < entry.getValue()) return ServiceResult.failure("库存不足: " + (p!=null?p.getName():entry.getKey()));
            totalAmount = totalAmount.add(BigDecimal.valueOf(p.getPrice()).multiply(BigDecimal.valueOf(entry.getValue())));
        }

        double originalBalance = 0.0;
        if (memberId > 0) {
            Member m = memberDAO.getMemberById(memberId);
            if (m == null) return ServiceResult.failure("会员不存在");
            if (BigDecimal.valueOf(m.getBalance()).compareTo(totalAmount) < 0) return ServiceResult.failure("余额不足");
            originalBalance = m.getBalance();
            if (!memberDAO.updateBalance(memberId, m.getBalance() - totalAmount.doubleValue())) return ServiceResult.failure("扣款失败");
        }

        try {
            Order order = new Order();
            order.setMemberId(memberId > 0 ? memberId : 0);
            order.setOrderType(OrderDAO.TYPE_PRODUCT); // 商品类型
            order.setAmount(totalAmount.doubleValue());
            order.setPaymentStatus(OrderDAO.STATUS_PAID);
            order.setOrderTime(DateUtils.now());
            if (!orderDAO.addOrder(order)) throw new Exception("订单写入失败");

            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                OrderProduct item = new OrderProduct();
                item.setOrderId(order.getOrderId());
                item.setProductId(entry.getKey());
                item.setQuantity(entry.getValue());
                orderProductDAO.addOrderProduct(item);
                productDAO.decreaseStock(entry.getKey(), entry.getValue());
            }
            return ServiceResult.success("交易成功: ¥" + totalAmount);
        } catch (Exception e) {
            if (memberId > 0) memberDAO.updateBalance(memberId, originalBalance); // 回滚
            return ServiceResult.failure("交易异常: " + e.getMessage());
        }
    }

    /**
     * 会员充值 (修复：使用正确的 OrderType)
     */
    public ServiceResult<Void> recharge(int memberId, double amount) {
        if (amount <= 0) return ServiceResult.failure("金额必须 > 0");
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) return ServiceResult.failure("会员不存在");

        double newBalance = member.getBalance() + amount;
        if (!memberDAO.updateBalance(memberId, newBalance)) {
            return ServiceResult.failure("余额更新失败");
        }

        // >>> 关键修复：记录充值流水 <<<
        try {
            Order order = new Order();
            order.setMemberId(memberId);
            // 这里使用了修复后 OrderDAO 中的常量，不会再报 "type=recharge" 错误
            order.setOrderType(OrderDAO.TYPE_RECHARGE);
            order.setAmount(amount);
            order.setOrderTime(DateUtils.now());
            order.setPaymentStatus(OrderDAO.STATUS_PAID);
            orderDAO.addOrder(order);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ServiceResult.success("充值成功！当前余额: ¥" + String.format("%.2f", newBalance));
    }
}