package cn.com.shopgroup.order.http.response;

import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.order.model.GbOrderGoodsRefundRecord;
import cn.com.shopgroup.order.model.GbOrderInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// 团长端-用户退款申请订单明细(订单维度, 与审核入参 OrderRefundInfoRequest 对齐)
@Data
public class LeaderRefundApplyResponse {
    // 订单号(审核回传 refundOrderGoodsMap 的 key)
    private String orderNo;
    // 下单时间(时间戳秒)
    private Integer addTime;
    // 订单状态: 0待支付 1待收货 2部分收货 3已提货 4已退款 5售后 6已取消
    private Integer orderStatus;
    // 会员昵称
    private String nickname;
    // 会员手机号
    private String mobile;
    // 团购活动名称
    private String groupName;
    // 自提点名称
    private String pointName;
    // 订单实付金额(单位:元)
    private Double payFee;
    // 帮卖订单价格(单位:元)
    private Double orderPrice;

    // ---------- 本次申请信息(来自最近一笔申请记录, 与审核端兜底口径一致) ----------
    // 退款类型: 1=退款(退待收货部分) 2=退货退款(退已收货部分)(审核回传 refundFlag)
    private Integer refundFlag;
    // 申请原因
    private String applyReason;
    // 补充原因
    private String extraReason;
    // 申请时间(时间戳秒)
    private Integer applyTime;
    // 本次申请退款总金额(单位:元, 申请记录落库值; 审核端发起退款优先按此金额)
    private Double applyRefundAmount;
    // 申请商品描述原文(如 "商品名-规格,申请退数量:2,退款金额:10.0;")
    private String refundGoodsMsg;

    // ---------- 申请商品行(仅匹配查询状态的商品行, 与审核 refundGoodsMap 对齐) ----------
    private List<LeaderRefundApplyGoodsResponse> goods;

    // 订单 + 最近一笔申请记录 组装响应
    public static LeaderRefundApplyResponse build(GbOrderInfo order, GbOrderGoodsRefundRecord applyRecord) {
        LeaderRefundApplyResponse resp = new LeaderRefundApplyResponse();
        resp.setOrderNo(order.getOrderNo());
        resp.setAddTime(order.getAddTime());
        resp.setOrderStatus(order.getStatus());
        resp.setNickname(order.getNickname());
        resp.setMobile(order.getMobile());
        resp.setGroupName(order.getGroupName());
        resp.setPointName(order.getPointName());
        resp.setPayFee(MoneyUtil.centToYuan(order.getPayFee() == null ? 0 : order.getPayFee()));
        resp.setOrderPrice(order.getOrderPrice());
        if (applyRecord != null) {
            resp.setRefundFlag(applyRecord.getRefundFlag());
            resp.setApplyReason(applyRecord.getActionReason());
            resp.setExtraReason(applyRecord.getExtraReason());
            resp.setApplyTime(applyRecord.getAddTime());
            resp.setApplyRefundAmount(MoneyUtil.centToYuan(applyRecord.getRefundAmount() == null ? 0 : applyRecord.getRefundAmount()));
            resp.setRefundGoodsMsg(applyRecord.getRefundGoodsMsg());
        }
        List<LeaderRefundApplyGoodsResponse> goodsList = new ArrayList<>();
        if (order.getGoodsInfoList() != null) {
            Integer refundFlag = applyRecord == null ? null : applyRecord.getRefundFlag();
            for (cn.com.shopgroup.order.model.GbOrderGoodsInfo goods : order.getGoodsInfoList()) {
                goodsList.add(new LeaderRefundApplyGoodsResponse(goods, refundFlag));
            }
        }
        resp.setGoods(goodsList);
        return resp;
    }
}
