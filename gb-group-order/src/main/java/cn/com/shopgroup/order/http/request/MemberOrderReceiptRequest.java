package cn.com.shopgroup.order.http.request;

import lombok.Data;

import java.util.List;

// 用户扫码核销请求(整单/部分核销)
@Data
public class MemberOrderReceiptRequest {
    // 订单号
    private String orderNo;
    // 核销(实际领取)自提点id
    private Long pointId;
    // 核销商品列表(部分核销时传, 整单核销不传/传空): id=订单商品表id, num=本次核销数量
    private List<OrderVerifyGoodsRequest> goodsList;
}
