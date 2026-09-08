package cn.com.shopgroup.order.controller.group;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.utils.TokenUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.order.constants.OrderStatusEnum;
import cn.com.shopgroup.order.http.request.MemberOrderListRequest;
import cn.com.shopgroup.order.http.request.MemberOrderRefundListRequest;
import cn.com.shopgroup.order.http.request.MemberOrderRefundRequest;
import cn.com.shopgroup.order.http.request.OrderRefundApplyRequest;
import cn.com.shopgroup.order.http.request.OrderRefundGoodsRequest;
import cn.com.shopgroup.order.http.response.OrderRefundRecordResponse;
import cn.com.shopgroup.order.http.response.OrderResponse;
import cn.com.shopgroup.order.http.response.RefundOrderInfoResponse;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderGoodsRefundRecord;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.model.GbRefundReason;
import cn.com.shopgroup.order.service.GbOrderGoodsRefundRecordService;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.order.service.GbRefundReasonService;
import cn.com.shopgroup.user.model.GbMemberInfo;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.model.GbOrgShopInfo;
import cn.com.shopgroup.user.service.GbMemberInfoService;
import cn.com.shopgroup.user.service.GbOrgMessageInfoService;
import cn.com.shopgroup.user.service.GbOrgPointInfoService;
import cn.com.shopgroup.user.service.GbOrgShopInfoService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//用户端订单
@RestController
@Slf4j
@RequestMapping("/order")
public class MemberOrderController {

    @Resource
    private GbOrderInfoService orderInfoService;

    @Resource
    private GbOrgPointInfoService pointService;

    @Resource
    private GbOrgMessageInfoService messageService;

    @Resource
    private GbMemberInfoService memberInfoService;
    @Resource
    private GbOrderGoodsRefundRecordService refundRecordService;

    @Resource
    private GbRefundReasonService refundReasonService;

    @Resource
    private WxMiniAccessTokenHelper helper;
    @Resource
    private GbOrgShopInfoService shopInfoService;

    // 用户订单列表（按订单状态/商品名称筛选, 分页查询）
    @PostMapping("/group/order/list")
    public JsonResult orderList(@RequestBody MemberOrderListRequest request) {
        log.info("用户端查询订单列表接口-参数request:{}", JSON.toJSONString(request));
        // 查询用户信息
        String token = TokenUtils.getToken();
        if (StringUtils.isEmpty(token)) {
            return JsonResult.fail("token不存在");
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            return JsonResult.fail("用户不存在");
        }
        Long memberId = 0l;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return JsonResult.fail("用户不存在");
        }
        if (memberId == 0) {
            return JsonResult.fail("用户不存在");
        }
        // 请求参数矫正
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize())
                .map(size -> Math.min(size, 100))
                .orElse(10);
        // 查询订单信息
        List<GbOrderInfo> list = orderInfoService.getMemberOrderList(memberId, request.getStatus(), request.getGoodsName(), page, pageSize);
        if (CollectionUtils.isEmpty(list)) {
            return JsonResult.success();
        }
        List<OrderResponse> data = OrderResponse.getOrderResponseList(list);
        return JsonResult.success(data);
    }

    // 用户售后订单列表（按订单状态筛选, 分页查询）
    @PostMapping("/group/order/applyRefundList")
    public JsonResult applyRefundList(@RequestBody MemberOrderRefundListRequest request) {
        log.info("用户端查询售后订单列表接口-参数request:{}", JSON.toJSONString(request));
        // 查询用户信息
        String token = TokenUtils.getToken();
        if (StringUtils.isEmpty(token)) {
            return JsonResult.fail("token不存在");
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            return JsonResult.fail("用户不存在");
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return JsonResult.fail("用户不存在");
        }
        if (memberId == 0) {
            return JsonResult.fail("用户不存在");
        }
        // 请求参数矫正
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize())
                .map(size -> Math.min(size, 100))
                .orElse(10);
        // 查询订单信息
        List<GbOrderInfo> list = orderInfoService.getMemberApplyRefundOrderList(memberId, request.getStatus(), page, pageSize);
        if (CollectionUtils.isEmpty(list)) {
            return JsonResult.success();
        }
        List<OrderResponse> data = OrderResponse.getOrderResponseList(list);
        return JsonResult.success(data);
    }


    // 用户订单数量
    @GetMapping("/group/order/count")
    public JsonResult orderList(@RequestParam("pid") Long pointId) {

        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            return JsonResult.fail("token不存在");
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            return JsonResult.fail("用户不存在");
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
            if (memberId == 0) {
                return JsonResult.fail("用户不存在");
            }
        } catch (NumberFormatException e) {
            return JsonResult.fail("用户不存在");
        }
        // 根据提货点ID查询团长ID
        Long leaderId = 0L;
        if (pointId > 0) {
            GbOrgPointInfo pointInfo = pointService.getPointInfo(pointId);
            leaderId = Optional.ofNullable(pointInfo).map(GbOrgPointInfo::getLeaderId).orElse(0l);
        }

        // 查询订单数量
        long total = orderInfoService.getMiniOrderCount(memberId, leaderId);
        return JsonResult.success(total);
    }

    // 用户订单详情
    @GetMapping("/group/order/info")
    public JsonResult orderInfo(@RequestParam("orderNo") String orderNo) {

        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            return JsonResult.fail("token不存在");
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            return JsonResult.fail("用户不存在");
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
            if (memberId == 0) {
                return JsonResult.fail("用户不存在");
            }
        } catch (NumberFormatException e) {
            return JsonResult.fail("用户不存在");
        }

        // 查询订单信息
        GbOrderInfo result = orderInfoService.getMiniOrderInfo(memberId, orderNo);
        if (ObjectUtils.isEmpty(result)) {
            return JsonResult.success();
        }
        OrderResponse data = new OrderResponse(result);
        return JsonResult.success(data);
    }

    // 用户订单小程序码(微信小程序码, 扫码进入C端小程序对应订单页面)
    @GetMapping("/group/order/makeErcode")
    public JsonResult orderMakeErcode(@RequestParam("orderNo") String orderNo) {

        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            return JsonResult.fail("token不存在");
        }
        String userId = TokenUtils.parseToken(token);
        if (StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            return JsonResult.fail("用户不存在");
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return JsonResult.fail("用户不存在");
        }
        if (memberId == 0) {
            return JsonResult.fail("用户不存在");
        }

        // 统一获取AccessToken
        String accessToken = helper.getAccessToken(false);
        if (accessToken == null || accessToken.length() == 0) return JsonResult.fail("获取AccessToken失败");

        // 生成小程序码, 返回base64格式
        String base64 = "";
        try {
            // 小程序码落地页与scene参数: 需与小程序前端onLoad解析保持一致
            String page = "pages/order/index";
            String scene = "orderNo=" + orderNo;
            int wh = 640; // 图片像素(最高1280像素)
            BufferedImage qrImg = WxMiniProgramHelper.getMiniProgramPageERcodeBufferedImage(accessToken, page, scene, wh);
            if (qrImg != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(qrImg, "png", baos);
                base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
            }
        } catch (Exception e) {
            log.error("生成小程序码失败：" + e.getMessage());
            e.printStackTrace();
        }

        // 返回
        if (base64.length() == 0) {
            return JsonResult.fail("二维码生成失败");
        } else {
            return JsonResult.success("二维码生成成功", base64);
        }
    }


    // 用户订单收货
    @GetMapping("/group/order/receipt")
    public JsonResult orderReceipt(@RequestParam("orderNo") String orderNo, @RequestParam("point") Long pointId) {

        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            return JsonResult.fail("token不存在");
        }
        String userId = TokenUtils.parseToken(token);
        if (StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            return JsonResult.fail("用户不存在");
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return JsonResult.fail("用户不存在");
        }
        if (memberId == 0) {
            return JsonResult.fail("用户不存在");
        }

        // 先查询订单信息
        GbOrderInfo orderInfo = orderInfoService.getMiniOrderInfo(memberId, orderNo);
        if (ObjectUtils.isEmpty(orderInfo)) {
            return JsonResult.fail("订单不存在");
        }

        int status = orderInfo.getStatus().intValue();
        // 支付状态, 未支付不能收货
        if (status == 0) {
            return JsonResult.fail("未支付, 不能收货");
        }

        // 退货状态, 申请退货不能收货
        if (status == 3 || status == 4) {
            return JsonResult.fail("已退款, 不能收货");
        }

        // 收货状态, 不能重复收货
        int receiptTime = orderInfo.getReceiptTime();
        if (status == 2 && receiptTime > 0) {
            return JsonResult.fail("不能重复收货");
        }

        // 实际收货点(自提点)
        String pointName = "";
        GbOrgPointInfo pointInfo = pointService.getPointInfo(pointId);
        if (pointInfo != null) pointName = pointInfo.getPointName();
        // 收货操作
        Boolean flag = orderInfoService.miniReceiptOrder(memberId, orderNo, pointId, pointName);
        // 收货消息类型: 1=系统消息2=内部消息3=业务消息
        // 员工-提货点绑定表 gb_org_point_staff 已下线, 不再通知店员, 直接通知团长
        String content = orderInfo.getNickname() + "(" + orderInfo.getMobile() + ")主动核销了编号 " + orderInfo.getReceiptCode() + " 的订单。";
        messageService.addMiniLeaderMessageInfo(orderInfo.getLeaderId(), 0L, (byte) 3, content);

        // 返回结果
        if (flag) {
            return JsonResult.success("收货成功");
        } else {
            return JsonResult.fail("收货失败");
        }
    }

    // 用户申请订单退款。refundFlag: 1=退款(退"待收货"部分, 可退量=购买数-收货数-已申请退款数), 2=退货退款(退"已收货"部分, 可退量=收货数-已申请退货退款数); 申请成功后订单refund_fee与对应商品行退款/退货退款数量先占坑累计(可退量会相应扣减), 待团长审核: 同意=保留占坑并转正式退款, 不同意=自动恢复申请前(扣回订单refund_fee、回退商品行数量、售后状态置不同意); 出参data为本次申请退款总金额(单位:元)
    @PostMapping("/group/order/apply/refund")
    public JsonResult orderApplyRefund(@Validated @RequestBody OrderRefundApplyRequest refundApplyRequest) {
        log.info("[用户申请订单退款操作],params->{}", JSON.toJSONString(refundApplyRequest));
        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            JsonResult.fail("token不存在");
        }
        String userId = TokenUtils.parseToken(token);
        if (StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            JsonResult.fail("用户不存在");
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return JsonResult.fail("用户不存在");
        }
        GbMemberInfo memberInfo = memberInfoService.getMemberInfo(memberId);
        if (ObjectUtils.isEmpty(memberInfo)) {
            return JsonResult.fail("用户不存在");
        }
        String memberName = memberInfo.getNickname();
        String orderNo = refundApplyRequest.getOrderNo();
        // 先查询订单信息
        GbOrderInfo orderInfo = orderInfoService.getMiniOrderInfo(memberId, orderNo);
        if (ObjectUtils.isEmpty(orderInfo)) {
            return JsonResult.fail("订单不存在");
        }

        int status = orderInfo.getStatus().intValue();
        // 支付状态, 未支付不能收货
        if (status == OrderStatusEnum.UNPAID.getCode()) {
            return JsonResult.fail("未支付, 不能申请退");
        }

        // 退货状态, 申请退货不能收货
        if (status == OrderStatusEnum.REFUNDED.getCode()) {
            return JsonResult.fail("已退款, 不能申请");
        }
        List<GbOrderGoodsInfo> goodsList = orderInfoService.getOrderGoodsList(orderNo);
        if (CollectionUtils.isEmpty(goodsList)) {
            return JsonResult.fail("该订单未查询到商品信息");
        }

        Map<Long, OrderRefundGoodsRequest> goodsMap = refundApplyRequest.getRefundGoodsMap();
        StringBuilder sb = new StringBuilder();
        // 本次申请退款的总金额(单位:元)
        Double allRefundAmount = 0D;
        //    退款(1): 可退"待收货(尚未收货)"部分 = 购买数量 - 收货数量 - 已申请退款数量
        //    退货退款(2): 可退"已收货"部分 = 收货数量 - 已申请退货退款数量
        int isReturnGoods = Optional.ofNullable(refundApplyRequest.getRefundFlag()).orElse(0).intValue();
        if (isReturnGoods != 1 && isReturnGoods != 2) {
            return JsonResult.fail("退款类型不正确");
        }
        // 本次申请实际处理的商品行数, 用于校验申请退款的商品都属于该订单
        int handledCount = 0;
        // 收集本次申请的商品行(仅这些行会累加退款/退货退款数量, 避免误加未申请的行)
        List<GbOrderGoodsInfo> applyGoodsList = new ArrayList<>();
        for (GbOrderGoodsInfo orderGoods : goodsList) {
            Long tempId = orderGoods.getId();
            String tempGoodsName = orderGoods.getGoodsName();
            // 总购买数
            int goodsNum = orderGoods.getGoodsNum() == null ? 0 : orderGoods.getGoodsNum();
            // 已收货数
            int receiptNum = orderGoods.getReceiptNum() == null ? 0 : orderGoods.getReceiptNum();
            // 退货退款数(退已收货部分, 申请累计)
            int refundedGoodNum = orderGoods.getRefundGoodsNum() == null ? 0 : orderGoods.getRefundGoodsNum();
            // 退款数(退待收货部分, 申请累计)
            int refundedNum = orderGoods.getRefundNum() == null ? 0 : orderGoods.getRefundNum();
            int canRefundNum = 0;
            if (isReturnGoods == 2) {
                // 退货退款: "已收货"但未申请退货退款的剩余数量
                if (receiptNum - refundedGoodNum > 0) {
                    canRefundNum = receiptNum - refundedGoodNum;
                }
            } else if (isReturnGoods == 1) {
                // 退款: "待收货(尚未收货)"但未申请退款的剩余数量
                if (goodsNum - receiptNum - refundedNum > 0) {
                    canRefundNum = goodsNum - receiptNum - refundedNum;
                }
            }
            // 本次申请范围外的商品行不参与校验
            if (!goodsMap.containsKey(tempId)) {
                continue;
            }
            OrderRefundGoodsRequest temp = goodsMap.get(tempId);
            Integer applyNum = temp.getRefundNum();
            if (applyNum == null || applyNum <= 0) {
                return JsonResult.fail("商品名称" + tempGoodsName + "申请退数量必须大于0，请核对后再提交");
            }
            if (canRefundNum <= 0) {
                return JsonResult.fail("商品名称" + tempGoodsName + "已无可退数量，请核对后再提交");
            }
            if (applyNum > canRefundNum) {
                return JsonResult.fail("商品名称" + tempGoodsName + "申请退数量大于可退数量，请核对后再提交");
            }
            handledCount++;
            // 加入本次申请商品行集合(后续仅这些行累加退款/退货退款数量)
            applyGoodsList.add(orderGoods);
            String decMsg = tempGoodsName + ",申请退数量:" + applyNum + ",退款金额:" + temp.getRefundAmount() + ";";
            sb.append(decMsg);
            // 累加各商品退款金额, 计算本次申请退款的总金额
            allRefundAmount += temp.getRefundAmount();
            // 同时标记订单商品售后状态待审核(内存值, 供下方累加使用)
            orderGoods.setApplyRefund(1);
            if (isReturnGoods == 1) {
                orderGoods.setRefundNum(applyNum);
            } else {
                orderGoods.setRefundGoodsNum(applyNum);
            }
        }
        // 申请退款的商品必须全部属于该订单
        if (handledCount != goodsMap.size()) {
            return JsonResult.fail("申请退款的商品信息与订单不一致，请重新提交");
        }

        String refundGoodsMsg = sb.toString();
        // 累加结果四舍五入精确到分, 避免浮点误差
        allRefundAmount = MoneyUtil.centToYuan(MoneyUtil.yuanToCent(allRefundAmount));
        // 申请退款总金额不能超过订单实付金额(payFee 单位:分), 防止超额退款
        int payFee = orderInfo.getPayFee() == null ? 0 : orderInfo.getPayFee().intValue();
        if (MoneyUtil.yuanToCent(allRefundAmount) > payFee) {
            return JsonResult.fail("申请退款总金额大于订单实付金额, 请重新申请");
        }

        // 申请退款 1 订单状态变成售后, 订单refund_fee累加本次申请金额(占坑)
        Boolean flag = orderInfoService.miniRefundOrder(memberId, orderNo, allRefundAmount);
        // 订单商品变更: 仅本次申请的商品行累加退款/退货退款数量(占坑); 审核同意保留, 审核拒绝时回退
        orderInfoService.updateOrderGoodsRefundByOrderNo(applyGoodsList, isReturnGoods);
        if (flag) {
            //记录申请售后日志
            GbOrderGoodsRefundRecord refundRecord = new GbOrderGoodsRefundRecord();
            refundRecord.setRefundGoodsMsg(refundGoodsMsg);
            refundRecord.setOperateId(memberId);
            refundRecord.setOperateName(memberName);
            refundRecord.setIsAgree(0);
            refundRecord.setOrderNo(orderNo);
            // 落库本次申请的类型与金额(单位:分), 团长端审核未回传类型/金额时, 拒绝流程据此兜底恢复
            refundRecord.setRefundFlag(isReturnGoods);
            refundRecord.setRefundAmount(MoneyUtil.yuanToCent(allRefundAmount));
            refundRecord.setActionReason(refundApplyRequest.getActionReason());
            refundRecord.setExtraReason(refundApplyRequest.getExtraReason());
            refundRecord.setAddTime(TimeUtils.getTimeStamp());
            refundRecordService.addRefundRecord(refundRecord);
            // 返回本次申请退款的总金额, 供前端展示
            return JsonResult.success("已申请退款", allRefundAmount);
        } else {
            return JsonResult.fail("申请退款失败");
        }

    }

    // 用户退款原因下拉列表(申请退款时"选择退款原因")
    @GetMapping("/group/order/refund/reasonList")
    public JsonResult refundReasonList() {
        log.info("用户退款原因下拉列表接口,开始");
        List<GbRefundReason> reasonList = refundReasonService.getEnabledReasonList();
        return JsonResult.success(reasonList);
    }

    // 申请售后记录查询
    @GetMapping("/group/order/refund/recodes")
    public JsonResult getRefundRecords(@RequestParam("orderNo") String orderNo) {
        log.info("申请售后记录查询,orderNo:{}", orderNo);
        List<GbOrderGoodsRefundRecord> recordList = refundRecordService.getRefundRecordListByOrderNo(orderNo);
        log.info("申请售后记录查询,返回recordList:{}", JSON.toJSONString(recordList));
        if (CollectionUtils.isEmpty(recordList)) {
            return JsonResult.success();
        }
        List<OrderRefundRecordResponse> result = OrderRefundRecordResponse.getOrderRefundRecordResponseList(recordList);
        return JsonResult.success(result);
    }


    //用户端-查询还有商品未全部收货的订单列表(该用户在该团长/店铺下已支付, 且存在商品行收货数量小于购买数量的订单)
    @GetMapping("/group/order/notAllReceiptList")
    public JsonResult notAllReceiptOrderList(@RequestParam(value = "shopId") Long shopId) {
        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            return JsonResult.fail("token不存在");
        }
        String userId = TokenUtils.parseToken(token);
        if (StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            return JsonResult.fail("用户不存在");
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return JsonResult.fail("用户不存在");
        }
        if (memberId == 0) {
            return JsonResult.fail("用户不存在");
        }
        GbOrgShopInfo shopInfo = shopInfoService.getByShopId(shopId);
        if (ObjectUtils.isEmpty(shopInfo)) {
            return JsonResult.fail("店铺信息不可用");
        }
        Long leaderId = shopInfo.getLeaderId();
        // 查询还有商品未全部收货的订单列表
        List<GbOrderInfo> list = orderInfoService.getNotAllReceiptOrderList(memberId, leaderId, shopId);
        if (CollectionUtils.isEmpty(list)) {
            return JsonResult.success();
        }
        List<OrderResponse> data = OrderResponse.getOrderResponseList(list);
        return JsonResult.success(data);
    }

    // 用户点击确认收货组件后调用接口，更新订单已经操作按钮
    @PostMapping("/group/order/confirmShipping")
    public JsonResult orderReceipt(@RequestParam("orderNo") String orderNo) {
        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            return JsonResult.fail("token不存在");
        }
        String userId = TokenUtils.parseToken(token);
        if (StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            return JsonResult.fail("用户不存在");
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return JsonResult.fail("用户不存在");
        }
        if (memberId == 0) {
            return JsonResult.fail("用户不存在");
        }

        orderInfoService.updateClickConfirmFlag(orderNo, 1);
        return JsonResult.success();
    }

    // 用户点击申请退货后调用接口
    @PostMapping("/group/order/applyRefund/orderInfo")
    public JsonResult applyRefundOrderInfo(@Validated @RequestBody MemberOrderRefundRequest request) {
        log.info("用户点击申请退货后查询订单信息req:{}", JSON.toJSONString(request));
        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            return JsonResult.fail("token不存在");
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            return JsonResult.fail("用户不存在");
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
            if (memberId == 0) {
                return JsonResult.fail("用户不存在");
            }
        } catch (NumberFormatException e) {
            return JsonResult.fail("用户不存在");
        }

        // 查询订单信息
        RefundOrderInfoResponse result = orderInfoService.getApplyRefundOrderInfo(memberId, request.getOrderNo(), request.getRefundFlag());
        return JsonResult.success(result);
    }


}
