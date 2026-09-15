package cn.com.shopgroup.order.service;

import cn.com.shopgroup.order.http.request.OrderRequest;
import java.util.Map;

public interface OrderService {

    // 小程序下单
    Map<String, String> addOrder(Long memberId, OrderRequest request);

    /**
     * 生成订单号（高并发安全）
     */
    String createOrderNo(Long memberId);

    // 生成核销码 只考虑每个团长下能够"唯一性"就行了
    // 避免相同时间不同用户下单生成相同订单编号
    String createOrderCode(Long leaderId);

}