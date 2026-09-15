package cn.com.shopgroup.service;

import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import java.util.List;
import java.util.Map;

public interface TaskOrderService {

    // 查询待系统自动完成收货的订单, 定时任务需要"已分账核销但核销满7天仍未完成收货"的订单自动收货掉
    // verifyEndTime: 核销时间截止点(当前时间-7天), 核销时间早于该值的未完成订单才会被查询出来
    List<Map<String, String>> getUnReceiptOrderIds(int verifyEndTime);

    /**
     * 系统自动完成收货(一个订单一次事务):
     * 把"已分账且已核销、但7天内未主动确认收货"的订单补成真实完成态, 与店员整单核销/用户主动收货后的数据口径保持一致:
     * 1) 订单主表: 待收货(1)/部分收货(2) -> 已收货(3), 补齐收货时间/确认收货标记/更新时间(条件更新防误伤退款/售后/取消单)
     * 2) 订单商品表: 把未收足的商品行收货数量补足为"可收货数量"(购买数-已申请退款/退货退款占坑数), 避免退款部分被重复核销
     */
    void receiptOrder(String orderNo, Integer receiptTime);

    // 查询未支付的订单, 用于返回商品库存
    List<GbOrderGoodsInfo> getUnPayOrderGoodsList(int time, int limit);

    /**
     * 超时未支付订单自动取消并恢复库存:
     * 下单超过指定时间(30分钟)仍未支付(pay_time=0)且仍为待支付(status=0)的订单,
     * 先抢占式把订单状态改为已取消(6), 抢单成功(0->6 恰好更新1行)后才回补该订单商品库存(商品总库存 + SKU库存),
     * 避免重复执行/并发触发导致同一订单被重复回补库存
     *
     * @param time  截止时间戳(下单时间小于该值即视为超时)
     * @param limit 本次最多处理的订单商品行数
     * @return 实际取消的订单数
     */
    int cancelTimeoutUnpaidOrderAndReturnStock(int time, int limit);

    // 将未支付订单状态改为已取消(CAS式更新, 只更新"待支付(status=0)且未支付(pay_time=0)"的订单)
    int cancelUnpaidOrder(String orderNo, Integer updateTime);

    /**
     * (Redis延迟队列驱动)取消超时未支付订单并恢复库存:
     * 与 cancelTimeoutUnpaidOrderAndReturnStock 的取消/回补逻辑一致, 区别是订单号来源为Redis延迟队列(下单时入队, 15分钟到期),
     * 无需全表扫描待支付订单; 抢占式取消(status 0->6 更新成功)成功后才回补库存, 已支付/已取消的订单不命中, 天然幂等
     *
     * @param orderNo 订单号
     * @return 是否实际取消了该订单(未支付且被本次成功置为已取消返回true)
     */
    boolean cancelUnpaidOrderByOrderNoAndReturnStock(String orderNo);

}