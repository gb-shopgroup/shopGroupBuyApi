package cn.com.shopgroup.service;

import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import java.util.List;

public interface BusinessOrderService {

    // 根据id查询商户订单
    GbOrderBusinessInfo getOrderBusinessInfo(String orderNo);

    // 查询待发货订单, 去掉退款订单
    List<GbOrderBusinessInfo> getUnSendBusinessOrderList(int limit);

    // 同步发货标识(CAS: 仅未发货的订单更新成功, 防止定时/手动重复调用微信发货后重复落库)
    boolean updateBusinessOrderSendStatus(String orderNo);

    // 查询订单是否被解冻
    List<GbOrderBusinessInfo> getFreezeBusinessOrderList(int limit);

    // 同步解冻标识(CAS: 仅未解冻的订单更新成功, 防止已解冻/已分账订单被重复回调时把commStatus倒退覆盖)
    boolean updateBusinessOrderFreezeStatus(String orderNo);

    // 查询待分账订单数量, 与 getUnDivideBusinessOrderList 同口径: 已解冻(核销)未分账且未退款的订单
    long getUnDivideBusinessOrderCount();

    // 查询待分账订单, 前提是已经微信解冻, 去掉退款订单
    List<GbOrderBusinessInfo> getUnDivideBusinessOrderList(int page, int pageSize);

    // 同步分账标识(CAS: 仅未分账的订单更新成功, 防止定时/手动重复触发对同一订单重复分账)
    Boolean updateBusinessOrderDivideStatus(String orderNo, String status, String no);

    // 查询待同步微信收货状态的订单: 近48小时内已调用微信发货(wx_shipment=1)且用户未确认收货(click_confirm_flag=0)的订单
    // 返回订单收款账户信息(含微信单号transactionId), 供定时任务向微信查询收货状态
    List<GbOrderBusinessInfo> getUnConfirmReceiptBusinessOrderList(int startTime, int limit);

    // 同步订单收货情况: 微信返回收货状态后, 标记订单已确认收货(click_confirm_flag=1)
    // CAS: 仅未确认收货的订单更新成功, 防止定时任务并发/重复触发重复落库
    boolean updateBusinessOrderReceiptStatus(String orderNo);

    // 同步发货状态(定时任务): 微信侧已发货(状态>=2)时补齐本地发货标识
    // CAS: 仅未发货且未退款的订单更新成功, 防止定时/手动重复调用微信发货后重复落库, 以及覆盖已解冻/已分账/已退款状态
    boolean syncBusinessOrderSendStatus(String orderNo);

    // 同步退款状态(定时任务): 微信侧已退款(状态=5)时同步本地数据状态
    // CAS: 仅未退款的订单更新成功, 保证重复触发时幂等
    boolean syncBusinessOrderRefundStatus(String orderNo);

}