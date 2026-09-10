package cn.com.shopgroup.order.http.response;

import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrderMainRefundResponse {

    // 订单信息
    private String orderNo;
    //订单金额
    private Double orderPrice;
    // 退款金额,单位：分
    private Integer refundFee;
    // 团长信息id
    private Long leaderId;
    // 团购信息
    private Long groupId;
    private String groupName;
    // 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消
    private Integer status;
    // 自提点信息
    private Long pointId;
    private String pointName;
    private String pointAddress;
    private String receiptCode;
    // 下单用户信息（内部调用使用）
    private String nickname;
    private String mobile;
    // 订单商品列表
    private List<OrderGoodsReponse> goods;

    List<OrderRefundRecordResponse> refundRecords;

    public OrderMainRefundResponse() {

    }

    public OrderMainRefundResponse(GbOrderInfo data) {

        // 订单信息
        this.orderNo = data.getOrderNo();
        this.orderPrice = data.getOrderPrice();

        // 团长信息和店铺名称
        this.leaderId = data.getLeaderId();

        // 团购信息
        this.groupId = data.getGroupId();
        this.groupName = data.getGroupName();
        this.status = data.getStatus();
        // 商品信息
        List<GbOrderGoodsInfo> goodsList = data.getGoodsInfoList();
        this.goods = OrderGoodsReponse.getOrderGoodsReponseList(goodsList);
        // 自提点信息
        this.pointId = data.getPointId();
        this.pointName = data.getPointName();
        this.pointAddress = data.getPointAddress();
        this.receiptCode = data.getReceiptCode();
        // 下单用户信息（内部调用使用）
        this.nickname = data.getNickname();
        this.mobile = data.getMobile();
    }

    // 列表转换
    public static List<OrderMainRefundResponse> getOrderResponseList(List<GbOrderInfo> lists) {

        List<OrderMainRefundResponse> data = new ArrayList<>();
        for (GbOrderInfo item : lists) {
            data.add(new OrderMainRefundResponse(item));
        }
        return data;
    }

}
