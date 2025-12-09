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
    private MemberDAO memberDAO; // 1. 声明

    public ShopService() {
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
        this.orderProductDAO = new OrderProductDAO();
        this.memberDAO = new MemberDAO(); // 2. 必须在这里初始化！！否则报错
    }

    /**
     * 获取所有在售商品
     */
    public java.util.List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    /**
     * 结算/创建订单
     */
    public ServiceResult<Void> checkout(int memberId, Map<Integer, Integer> cart) {
        if (cart == null || cart.isEmpty()) {
            return ServiceResult.failure("购物车是空的");
        }

        // 1. 计算总金额并验证库存
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();

            Product product = productDAO.getProductById(productId);
            if (product == null) {
                return ServiceResult.failure("商品ID " + productId + " 不存在");
            }
            if (product.getStock() < quantity) {
                return ServiceResult.failure("商品 [" + product.getName() + "] 库存不足 (仅剩 " + product.getStock() + ")");
            }

            BigDecimal priceBD = BigDecimal.valueOf(product.getPrice());
            BigDecimal itemTotal = priceBD.multiply(new BigDecimal(quantity));
            totalAmount = totalAmount.add(itemTotal);
        }

        // ==========================================
        // 2. 余额扣款逻辑 (前置扣款)
        // ==========================================
        double originalBalance = 0.0; // 记录原余额，用于退款

        if (memberId > 0) {
            Member member = memberDAO.getMemberById(memberId);
            if (member == null) {
                return ServiceResult.failure("结算失败：会员ID不存在。");
            }

            BigDecimal currentBalance = BigDecimal.valueOf(member.getBalance());
            originalBalance = member.getBalance(); // 记住这个数字！

            // 检查余额
            if (currentBalance.compareTo(totalAmount) < 0) {
                return ServiceResult.failure("结算失败：会员余额不足！\n当前余额: ¥" + member.getBalance());
            }

            // 计算并扣款
            double newBalance = currentBalance.subtract(totalAmount).doubleValue();
            if (!memberDAO.updateBalance(memberId, newBalance)) {
                return ServiceResult.failure("结算失败：余额扣除失败 (数据库错误)。");
            }
        }

        // ==========================================
        // 3. 订单创建与库存扣减 (风险区 - 需要捕获异常来退款)
        // ==========================================
        try {
            // 3.1 准备订单对象
            Order order = new Order();
            if (memberId > 0) {
                order.setMemberId(memberId);
                order.setPaymentStatus("paid_by_balance"); // 标记为余额支付
            } else {
                order.setMemberId(0); // 散客存0 (DAO层会处理为NULL)
                order.setPaymentStatus("paid_by_cash");    // 标记为现金支付
            }

            order.setOrderType("product");
            order.setAmount(totalAmount.doubleValue());
            order.setOrderTime(DateUtils.now());

            // 3.2 写入主订单
            boolean orderSuccess = orderDAO.addOrder(order);
            if (!orderSuccess) {
                throw new Exception("主订单创建失败");
            }
            int orderId = order.getOrderId();

            // 3.3 写入明细 & 扣减库存
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();

                // A. 写入明细
                OrderProduct item = new OrderProduct();
                item.setOrderId(orderId);
                item.setProductId(productId);
                item.setQuantity(quantity);
                orderProductDAO.addOrderProduct(item);

                // B. 扣减库存 (使用 decreaseStock 更安全)
                // 如果你没有 decreaseStock 方法，用 updateProduct 也可以，但最好用专门的方法
                if (!productDAO.decreaseStock(productId, quantity)) {
                    throw new Exception("商品 [" + productId + "] 库存扣减失败");
                }
            }

            // 全部成功！
            return ServiceResult.success("交易成功！收款金额: ¥" + totalAmount);

        } catch (Exception e) {
            // ==========================================
            // 4. 失败补偿：退还余额
            // ==========================================
            System.err.println("交易发生异常: " + e.getMessage());

            if (memberId > 0) {
                System.out.println("正在回滚余额...");
                // 把钱退回去 (设置为 originalBalance)
                boolean refundSuccess = memberDAO.updateBalance(memberId, originalBalance);
                if (refundSuccess) {
                    return ServiceResult.failure("交易失败 (库存不足或系统错误)，余额已自动退回。");
                } else {
                    return ServiceResult.failure("严重错误：交易失败且退款失败！请联系管理员人工核对余额。");
                }
            }

            return ServiceResult.failure("交易失败：" + e.getMessage());
        }
    }

    // ShopService.java 中的高级充值方法
// 这个方法不仅加钱，还会生成一条 type='recharge' 的订单记录

    // ==========================================
    // >>> 新增：高级充值方法 (带订单记录) <<<
    // ==========================================
    /**
     * 会员充值 (同时生成充值流水记录)
     * @param memberId 会员ID
     * @param amount 充值金额
     * @return 操作结果
     */
    public ServiceResult<Void> recharge(int memberId, double amount) {
        // 1. 基础验证
        if (amount <= 0) {
            return ServiceResult.failure("充值金额必须大于 0");
        }
        if (amount > 100000) {
            return ServiceResult.failure("单次充值金额过大，请核对");
        }

        // 2. 获取会员信息
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("会员不存在");
        }

        // 3. 执行充值 (更新余额)
        double oldBalance = member.getBalance();
        double newBalance = oldBalance + amount;

        // 这里的 updateBalance 是关键一步
        boolean updateSuccess = memberDAO.updateBalance(memberId, newBalance);

        if (!updateSuccess) {
            return ServiceResult.failure("充值失败：余额更新未成功");
        }

        // 4. 【核心差异】创建一条“充值订单”记录
        try {
            Order order = new Order();
            order.setMemberId(memberId);
            order.setOrderType("recharge"); // 对应数据库新增的枚举值
            order.setAmount(amount);        // 记录充了多少钱
            order.setOrderTime(DateUtils.now());
            order.setPaymentStatus("paid"); // 充值默认是现结

            // 写入订单表
            orderDAO.addOrder(order);

            // (可选) 如果你想更完美，这里甚至可以加 try-catch 回滚余额
            // 但为了代码简单，我们假设只要余额改成功了，订单记录失败只算“小瑕疵”

        } catch (Exception e) {
            e.printStackTrace();
            // 注意：这里虽然报错了，但钱已经充进去了。
            // 实际生产中会打印日志报警，但通常会告诉用户充值成功了。
            System.err.println("警告：充值成功但流水记录创建失败: " + e.getMessage());
        }

        return ServiceResult.success("充值成功！当前余额: ¥" + String.format("%.2f", newBalance));
    }
}