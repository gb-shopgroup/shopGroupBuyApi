package cn.com.shopgroup.order.http.response;

import cn.com.shopgroup.order.model.GbOrderGoodsRefundRecord;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// 订单申请售后记录信息
@Data
public class OrderRefundRecordResponse {
    // id主键自增
    private Long id;
    // 订单号
    private String orderNo;
    //退款商品描述
    private String refundGoodsMsg;
    // 操作人id
    private Long operateId;
    // 操作人姓名
    private String operateName;
    //状态 0 待审核 1 同意 2 不同意
    private Integer isAgree;
    // 申请原因
    private String actionReason;
    // 补充原因
    private String extraReason;
    // 添加时间
    private Integer addTime;

    public OrderRefundRecordResponse(GbOrderGoodsRefundRecord data) {
        this.id = data.getId();
        this.orderNo = data.getOrderNo();
        this.refundGoodsMsg = data.getRefundGoodsMsg();
        this.operateId = data.getOperateId();
        this.operateName = data.getOperateName();
        this.isAgree = data.getIsAgree();
        this.actionReason = data.getActionReason();
        this.extraReason = data.getExtraReason();
        this.addTime = data.getAddTime();
    }

    // 列表转化
    public static List<OrderRefundRecordResponse> getOrderRefundRecordResponseList(List<GbOrderGoodsRefundRecord> lists) {

        List<OrderRefundRecordResponse> data = new ArrayList<>();
        for (GbOrderGoodsRefundRecord item : lists) {
            data.add(new OrderRefundRecordResponse(item));
        }
        return data;
    }
}
