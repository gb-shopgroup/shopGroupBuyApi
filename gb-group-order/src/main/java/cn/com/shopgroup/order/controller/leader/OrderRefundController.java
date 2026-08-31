package cn.com.shopgroup.order.controller.leader;

import cn.com.shopgroup.common.utils.CustomIdGenerator;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.yeepay.YeepayUtils;
import cn.com.shopgroup.order.constants.PaymentStatusEnum;
import cn.com.shopgroup.order.http.request.OrderApproveRequest;
import cn.com.shopgroup.order.http.request.OrderRefundGoodsRequest;
import cn.com.shopgroup.order.http.request.OrderRefundInfoRequest;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderGoodsRefundRecord;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.model.OrderTransactionLog;
import cn.com.shopgroup.order.service.GbOrderBusinessInfoService;
import cn.com.shopgroup.order.service.GbOrderGoodsRefundRecordService;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.order.service.OrderTransactionLogService;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import cn.com.shopgroup.user.service.GbOrgMessageInfoService;
import cn.com.shopgroup.user.service.GbOrgStaffInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import com.alibaba.fastjson2.JSON;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@Api(value = "团长端--退款管理")
@RequestMapping("/order")
public class OrderRefundController {

    @Resource
    private GbOrderInfoService orderInfoService;

    @Resource
    private GbOrderBusinessInfoService orderBusinessInfoService;

    @Resource
    private GbOrgStaffInfoService staffService;

    @Resource
    private GbOrgMessageInfoService messageService;

    @Resource
    private GbOrderBusinessInfoService businessService;
    @Resource
    private GbOrderGoodsRefundRecordService refundRecordService;
    @Resource
    private OrderTransactionLogService transactionLogService;

    // 退款订单数量
    @GetMapping("/leader/refund/count")
    public JsonResult refundOrderCount(@RequestParam("gid") Long groupId, @RequestParam("pid") Long pointId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }

        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        if (staffId == 0) {
            return JsonResult.fail("sid不存在");
        }

        // 查询总数
        Long total = orderInfoService.getMiniLeaderOrderCount(leaderId, groupId, pointId, 4);
        return JsonResult.success(total);
    }

    //售后订单审核（同意/不同意）
    @PostMapping("/leader/refund/approve")
    public JsonResult approveRefundOrder(@Validated @RequestBody OrderApproveRequest approveRequest) {
        log.info("团长管理-审核退款订单处理.../order/refund/approve,参数:{}", JSON.toJSONString(approveRequest));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        if (staffId == 0) {
            return JsonResult.fail("sid不存在");
        }
        // 操作人员
        GbOrgStaffInfo staffInfo = staffService.getStaffInfo(staffId);
        if (ObjectUtils.isEmpty(staffInfo)) {
            return JsonResult.fail("staffId=" + staffId + ",没查询到操作人员");
        }
        // 审核结果 1 同意 2 拒绝
        Integer approveStatus = approveRequest.getStatus();
        String reason = approveRequest.getReason();
        //参数设置校验，这里不用判断空map情况
        Map<String, OrderRefundInfoRequest> refundMap = approveRequest.getRefundOrderGoodsMap();
        if (refundMap.isEmpty()) {
            return JsonResult.fail("请查看请求参数，审核失败");
        }
        for (Map.Entry<String, OrderRefundInfoRequest> entry : refundMap.entrySet()) {
            String orderNo = entry.getKey();
            OrderRefundInfoRequest refundInfoRequest = entry.getValue();
            // 先查询订单
            GbOrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);
            if (ObjectUtils.isEmpty(orderInfo)) {
                log.warn("订单不存在，审核失败");
                continue;
            }
            //查询原订单中的商品
            List<GbOrderGoodsInfo> goodsList = orderInfoService.getOrderGoodsList(orderNo);
            if (CollectionUtils.isEmpty(goodsList)) {
                return JsonResult.fail("该订单未查询到商品信息");
            }
            // 订单商品处理
            Map<Long, OrderRefundGoodsRequest> refundGoodsMap = refundInfoRequest.getRefundGoodsMap();
            for (GbOrderGoodsInfo goods : goodsList) {
                //未核销商品数量
                Long orderGoodsId = goods.getId();
                Integer goodNum = goods.getGoodsNum(); // 订单商品数量
                Integer refundedNum = goods.getRefundGoodsNum();// 已退货数量
                //如果购买数量小于退货数量，有误，返回；
                int remainRefundNum = goodNum.intValue() - refundedNum.intValue();
                if (remainRefundNum <= 0) {
                    return JsonResult.fail("申请退货商品数量大于总购买订单商品数目，审核失败");
                }
                if (refundGoodsMap.containsKey(orderGoodsId)) {
                    OrderRefundGoodsRequest temp = refundGoodsMap.get(orderGoodsId);
                    if (temp.getRefundNum() > remainRefundNum) {
                        return JsonResult.fail("申请退货商品数量大于总购买订单商品数目，审核失败");
                    }
                }
            }
            //同意处理
            if (approveStatus == 1) {
                int m = handleAgree(staffInfo, orderInfo, refundInfoRequest, reason);
                if (m == 0) {
                    continue;
                }
            } else {
                //拒绝
                handleRefuse(staffInfo, refundInfoRequest, reason);
            }
        }
        // 返回
        return JsonResult.success("审核成功");
    }

    //拒绝（不同意）处理
    public void handleRefuse(GbOrgStaffInfo staffInfo, OrderRefundInfoRequest request, String reason) {
        String orderNo = request.getOrderNo();
        String staffName = staffInfo.getStaffName();
        // 拒绝退款
        orderInfoService.editMiniLeaderRefundOrder(orderNo, staffName, reason);
        //拒绝请求过来的订单商品
        List<Long> orderGoodsIds = new ArrayList<>();
        for (Map.Entry<Long, OrderRefundGoodsRequest> entry : request.getRefundGoodsMap().entrySet()) {
            Long orderGoodsId = entry.getKey();
            orderGoodsIds.add(orderGoodsId);
        }
        //标识订单商品售后状态不同意
        orderInfoService.updateOrderGoodsApplyStatus(orderNo, orderGoodsIds, 3);
        //插入退货记录售后日志
        GbOrderGoodsRefundRecord refundRecord = new GbOrderGoodsRefundRecord();
        refundRecord.setOperateId(staffInfo.getStaffId());
        refundRecord.setOperateName(staffInfo.getStaffName());
        refundRecord.setIsAgree(2);//不同意
        refundRecord.setOrderNo(orderNo);
        refundRecord.setActionReason(reason);
        refundRecord.setAddTime(TimeUtils.getTimeStamp());
        refundRecordService.addRefundRecord(refundRecord);
        // 同步分账订单表, 核销之后的订单才能分账, 这样只要不核销订单, 就可以随时退款
        businessService.updateBusinessOrderCheckStatus(request.getOrderNo());

        // 添加日志, 消息类型: 1=系统消息2=内部消息3=业务消息
        Byte type = 2;
        String oper = "拒绝了";
        String content = staffName + " " + oper + " 订单号(" + orderNo + ") 的退款订单。";
        messageService.addMiniLeaderMessageInfo(staffInfo.getLeaderId(), staffInfo.getStaffId(), type, content);
    }

    //同意退款处理
    public int handleAgree(GbOrgStaffInfo staffInfo, GbOrderInfo orderInfo, OrderRefundInfoRequest request, String reason) {
        // 申请退款账户
        String merchantNo = orderInfo.getMerchantNo();
        String orderNo = orderInfo.getOrderNo();
        // 订单金额, 分转元
        double amount = MoneyUtil.centToYuan(orderInfo.getPayFee());
        Map<String, String> res = YeepayUtils.refund(merchantNo, orderNo, String.valueOf(amount));
        //插入交易流水表
        OrderTransactionLog transactionLog = new OrderTransactionLog();
        transactionLog.setOrderNo(orderNo);
        transactionLog.setTransactionNo("tr"+CustomIdGenerator.generateUUID());
        transactionLog.setPayAmount(amount);
        transactionLog.setPayMethod("yeePay");
        transactionLog.setOperatorId(staffInfo.getStaffId());
        transactionLog.setOperatorName(staffInfo.getStaffName());
        transactionLog.setAddTime(TimeUtils.getTimeStamp());
        transactionLog.setPayStatus(PaymentStatusEnum.REFUNDED.getCode());
        // 查看是否成功
        if (Integer.parseInt(res.get("success")) == 0) {
            log.error("退款失败：" + res.get("data"));
            transactionLog.setRemark("退款失败");
            handleInsertTransaction(transactionLog);
            return 0;
        } else {
            // 同步原始订单表和商户订单表的退款状态
            orderBusinessInfoService.editMiniLeaderOrderBusinessRefundStatus(orderNo);
            //查询该订单下是否有没有退款商品。如果没有，就改订单状态：退货，否则不改
            List<Long> orderGoodsIds = new ArrayList<>();
            for (Map.Entry<Long, OrderRefundGoodsRequest> entry : request.getRefundGoodsMap().entrySet()) {
                Long orderGoodsId = entry.getKey();
                orderGoodsIds.add(orderGoodsId);
            }
            //标识订单商品售后状态同意
            orderInfoService.updateOrderGoodsApplyStatus(orderNo, orderGoodsIds, 2);
            //插入退货记录售后日志
            GbOrderGoodsRefundRecord refundRecord = new GbOrderGoodsRefundRecord();
            refundRecord.setOperateId(staffInfo.getStaffId());
            refundRecord.setOperateName(staffInfo.getStaffName());
            refundRecord.setIsAgree(1);//同意退款
            refundRecord.setOrderNo(orderNo);
            refundRecord.setActionReason(reason);
            refundRecord.setAddTime(TimeUtils.getTimeStamp());
            refundRecordService.addRefundRecord(refundRecord);
            int flag = orderInfoService.getOrderGoodsStatus(orderNo);
            if (flag == 0) {
                //改订单商品状态 全部商品都退了为退款
                orderInfoService.editMiniLeaderRefundOrder(orderNo);
            }
            // 同步分账订单表, 核销之后的订单才能分账, 这样只要不核销订单, 就可以随时退款
            businessService.updateBusinessOrderCheckStatus(request.getOrderNo());

            // 添加日志, 消息类型: 1=系统消息2=内部消息3=业务消息
            byte type = 2;
            String oper = "通过了";
            String content = staffInfo.getStaffName() + " " + oper + " 订单号(" + orderNo + ") 的退款订单。";
            messageService.addMiniLeaderMessageInfo(staffInfo.getLeaderId(), staffInfo.getStaffId(), type, content);
            // 返回 成功标识
            transactionLog.setRemark("退款成功");
            handleInsertTransaction(transactionLog);
            return 1;
        }

    }

    private void handleInsertTransaction(OrderTransactionLog transactionLog) {
        transactionLogService.addTransactionLog(transactionLog);
    }

}
