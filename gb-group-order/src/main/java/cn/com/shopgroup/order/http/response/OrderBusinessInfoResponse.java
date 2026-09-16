package cn.com.shopgroup.order.http.response;

import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单收款账户信息（金额统一为 Double，单位：元）
 * 字段名与实体 GbOrderBusinessInfo 完全一致, 仅金额字段由「分」改为「元」
 */
@Data
public class OrderBusinessInfoResponse {

    // id,主键
    private Long Id;
    // 订单号
    private String orderNo;
    // 账户id,外键
    private Long busId;
    // 易宝商户编号,冗余
    private String merchantNo;
    // 团长id,外键
    private Long leaderId;
    // 团购名称,冗余
    private String groupName;
    // 订单编号,冗余
    private String orderSn;
    // 是否发货,微信发货
    private Byte isSend;
    // 微信单号,微信发货
    private String transactionId;
    // openid,微信发货
    private String openid;
    // 发货时间,微信发货
    private Integer sendTime;
    // 是否解冻,微信冻结
    private Byte isUnfreeze;
    // 解冻时间,微信冻结
    private Integer unfreezeTime;
    // 是否分账
    private Byte isDivide;
    // 分账状态
    private String divideStatus;
    // 分账时间
    private Integer divideTime;
    // 分账流水号
    private String divideNo;
    // 订单金额，单位元
    private Double orderFee;
    // 实到金额，单位元
    private Double receivedFee;
    // 分账金额，单位元
    private Double busFee;
    // 平台服务费，单位元
    private Double serviceFee;
    // 其他佣金，单位元
    private Double otherFee;
    // 数据状态: 0=已支付(支付回调),1=已发货(定时任务),2=已解冻(t+2),3=已分账(定时任务),4=已提现(用户申请),5=已退款(用户申请)
    private Byte commStatus;
    // 添加时间(下单时间)
    private Integer addTime;

    public OrderBusinessInfoResponse() {
    }

    public OrderBusinessInfoResponse(GbOrderBusinessInfo data) {

        this.Id = data.getId();
        this.orderNo = data.getOrderNo();
        this.busId = data.getBusId();
        this.merchantNo = data.getMerchantNo();
        this.leaderId = data.getLeaderId();
        this.groupName = data.getGroupName();
        this.orderSn = data.getOrderSn();
        this.isSend = data.getIsSend();
        this.transactionId = data.getTransactionId();
        this.openid = data.getOpenid();
        this.sendTime = data.getSendTime();
        this.isUnfreeze = data.getIsUnfreeze();
        this.unfreezeTime = data.getUnfreezeTime();
        this.isDivide = data.getIsDivide();
        this.divideStatus = data.getDivideStatus();
        this.divideTime = data.getDivideTime();
        this.divideNo = data.getDivideNo();
        // 金额字段: 分 -> 元
        this.orderFee = MoneyUtil.centToYuan(data.getOrderFee());
        this.receivedFee = MoneyUtil.centToYuan(data.getReceivedFee());
        this.busFee = MoneyUtil.centToYuan(data.getBusFee());
        this.serviceFee = MoneyUtil.centToYuan(data.getServiceFee());
        this.otherFee = MoneyUtil.centToYuan(data.getOtherFee());
        this.commStatus = data.getCommStatus();
        this.addTime = data.getAddTime();
    }

    // 列表转化
    public static List<OrderBusinessInfoResponse> getResponseList(List<GbOrderBusinessInfo> lists) {

        List<OrderBusinessInfoResponse> data = new ArrayList<>();
        if (lists == null) {
            return data;
        }
        for (GbOrderBusinessInfo item : lists) {
            data.add(new OrderBusinessInfoResponse(item));
        }
        return data;
    }
}
