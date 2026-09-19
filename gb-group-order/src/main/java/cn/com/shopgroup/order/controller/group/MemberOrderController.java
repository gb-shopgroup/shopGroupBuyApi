package cn.com.shopgroup.order.controller.group;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.utils.TokenUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.order.constants.OrderStatusEnum;
import cn.com.shopgroup.order.exception.OrderErrorCodeEnum;
import cn.com.shopgroup.order.http.request.MemberOrderListRequest;
import cn.com.shopgroup.order.http.request.MemberOrderReceiptRequest;
import cn.com.shopgroup.order.http.request.MemberOrderRefundListRequest;
import cn.com.shopgroup.order.http.request.MemberOrderRefundRequest;
import cn.com.shopgroup.order.http.request.OrderRefundApplyRequest;
import cn.com.shopgroup.order.http.request.OrderRefundGoodsRequest;
import cn.com.shopgroup.order.http.request.OrderVerifyGoodsRequest;
import cn.com.shopgroup.order.http.response.OrderMainRefundResponse;
import cn.com.shopgroup.order.http.response.OrderRefundRecordResponse;
import cn.com.shopgroup.order.http.response.OrderResponse;
import cn.com.shopgroup.order.http.response.OrderVerifyRecordResponse;
import cn.com.shopgroup.order.http.response.RefundOrderInfoResponse;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderGoodsRefundRecord;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.model.GbOrderVerifyRecord;
import cn.com.shopgroup.order.model.GbRefundReason;
import cn.com.shopgroup.order.service.GbOrderBusinessInfoService;
import cn.com.shopgroup.order.service.GbOrderGoodsRefundRecordService;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.order.service.GbOrderVerifyRecordService;
import cn.com.shopgroup.order.service.GbRefundReasonService;
import cn.com.shopgroup.user.model.GbMemberInfo;
import cn.com.shopgroup.user.model.GbOrgLeaderInfo;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.model.GbOrgShopInfo;
import cn.com.shopgroup.user.service.GbMemberInfoService;
import cn.com.shopgroup.user.service.GbOrgLeaderInfoService;
import cn.com.shopgroup.user.service.GbOrgMessageInfoService;
import cn.com.shopgroup.user.service.GbOrgPointInfoService;
import cn.com.shopgroup.user.service.GbOrgShopInfoService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    private GbOrderVerifyRecordService verifyRecordService;

    @Resource
    private GbRefundReasonService refundReasonService;

    @Resource
    private WxMiniAccessTokenHelper helper;

    @Resource
    private RedisHelper redisHelper;
    @Resource
    private GbOrgShopInfoService shopInfoService;

    @Resource
    private GbOrderBusinessInfoService businessService;

    @Resource
    private GbOrgLeaderInfoService leaderInfoService;
    @Resource
    private WxMiniAccessTokenHelper wxAccessTokenHelper;

    // 用户订单列表（按订单状态/商品名称筛选, 分页查询）
    @PostMapping("/group/order/list")
    public JsonResult orderList(@RequestBody MemberOrderListRequest request) {
        log.info("用户端查询订单列表接口-参数request:{}", JSON.toJSONString(request));
        // 查询用户信息
        String token = TokenUtils.getToken();
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(OrderErrorCodeEnum.TOKEN_NOT_EXIST);
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        Long memberId = 0l;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        if (memberId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        // 请求参数矫正
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize())
                .map(size -> Math.min(size, 20))
                .orElse(10);
        // 查询订单信息
        List<GbOrderInfo> list = orderInfoService.getMemberOrderList(memberId, request.getStatus(), request.getGoodsName(), page, pageSize);
        if (CollectionUtils.isEmpty(list)) {
            return JsonResult.success();
        }
        List<OrderResponse> data = OrderResponse.getOrderResponseList(list);
        log.info("用户端查询订单列表接口返回data:{}", JSON.toJSONString(data));
        return JsonResult.success(data);
    }

    // 用户售后订单列表(支持按商品名称/售后审核状态筛选; 同一订单的商品可能处于不同售后状态, 按(订单,审核状态)拆分为多条返回, 每条goods仅含该状态的商品, 行状态见goods[].applyRefund; 分页在订单维度)
    @PostMapping("/group/order/applyRefundList")
    public JsonResult applyRefundList(@RequestBody MemberOrderRefundListRequest request) {
        log.info("用户端查询售后订单列表接口-参数request:{}", JSON.toJSONString(request));
        // 查询用户信息
        String token = TokenUtils.getToken();
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(OrderErrorCodeEnum.TOKEN_NOT_EXIST);
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        if (memberId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        // 请求参数矫正
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize())
                .map(size -> Math.min(size, 20))
                .orElse(10);
        // 查询订单信息(支持商品名称过滤, 并按商品售后状态拆分)
        List<GbOrderInfo> list = orderInfoService.getMemberApplyRefundOrderList(
                memberId, request.getStatus(), request.getGoodsName(), page, pageSize);
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
            throw new BusinessException(OrderErrorCodeEnum.TOKEN_NOT_EXIST);
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
            if (memberId == 0) {
                throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
            }
        } catch (NumberFormatException e) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
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
            throw new BusinessException(OrderErrorCodeEnum.TOKEN_NOT_EXIST);
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
            if (memberId == 0) {
                throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
            }
        } catch (NumberFormatException e) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }

        // 查询订单信息
        GbOrderInfo result = orderInfoService.getMiniOrderInfo(memberId, orderNo);
        if (ObjectUtils.isEmpty(result)) {
            return JsonResult.success();
        }
        OrderResponse data = new OrderResponse(result);
        // 核销记录(支持一单多次部分核销, 按核销时间正序; 未核销过的订单返回空列表)
        List<GbOrderVerifyRecord> verifyRecordList = verifyRecordService.getVerifyRecordListByOrderNo(orderNo);
        data.setVerifyRecords(OrderVerifyRecordResponse.getOrderVerifyRecordResponseList(verifyRecordList));
        return JsonResult.success(data);
    }

    // 用户订单小程序码(微信小程序码, 扫码进入C端小程序对应订单页面)
    @GetMapping("/group/order/makeErcode")
    public JsonResult orderMakeErcode(@RequestParam("orderNo") String orderNo) {
        log.info("生成订单二维码,orderNo:{}", orderNo);
        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            throw new BusinessException(OrderErrorCodeEnum.TOKEN_NOT_EXIST);
        }
        String userId = TokenUtils.parseToken(token);
        if (StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        if (memberId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        //查询订单是否存在，订未支付/取消 状态，不让生成
        GbOrderInfo orderInfo = orderInfoService.getMiniOrderInfo(memberId, orderNo);
        log.info("生成订单二维码,orderNo:{},info:{}", orderNo, JSON.toJSONString(orderInfo));
        if (ObjectUtils.isEmpty(orderInfo)) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_NOT_EXIST);
        }
        int orderStatus = orderInfo.getStatus().intValue();
        if (orderStatus == 0 || orderStatus == 6) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_CANCELED_UNPAID_NOT_CODE);
        }
        // 小程序码永久有效: 优先读缓存, 生成一次后永久复用, 不再依赖access_token与微信接口
        String cacheKey = RedisConstant.WxMiniOrderErCodeKey + orderNo;
        String cached = redisHelper.getCacheObject(cacheKey);
        if (cached != null && cached.length() > 0) {
            return JsonResult.success("二维码生成成功", cached);
        }

        // 生成小程序码(access_token失效自动强刷重试), 返回base64格式
        String base64;
        try {
            // 小程序码落地页与scene参数: 需与小程序前端onLoad解析保持一致
            String page = "pagesA/order/detail";
            String scene = "orderNo=" + orderNo;
            int wh = 640; // 图片像素(最高1280像素)
            byte[] qrBytes = helper.getWxaCodeUnlimitWithRetry(page, scene, wh);
            if (qrBytes == null || qrBytes.length == 0) {
                log.error("生成小程序码失败 orderNo:{}", orderNo);
                throw new BusinessException(OrderErrorCodeEnum.ERCODE_GEN_FAILED);
            }
            // 统一转png编码输出
            BufferedImage qrImg = ImageIO.read(new ByteArrayInputStream(qrBytes));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImg, "png", baos);
            base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成小程序码失败 orderNo:{}", orderNo, e);
            throw new BusinessException(OrderErrorCodeEnum.ERCODE_GEN_FAILED);
        }

        // 生成成功后-24小时-缓存, 之后直接返回缓存(小程序码内容不变)
        redisHelper.setCacheObject(cacheKey, base64, 24L, TimeUnit.HOURS);

        // 返回
        return JsonResult.success("二维码生成成功", base64);
    }


    // 用户扫码核销-整单核销(GET): 核销订单全部剩余可核销商品(购买数-已核销-已退)
    @GetMapping("/group/order/receipt")
    public JsonResult orderReceipt(@RequestParam("orderNo") String orderNo, @RequestParam("point") Long pointId) {
        // 查询用户信息
        Long memberId = getLoginMemberId();
        // 整单核销(goodsMap传null): 核销全部剩余可核销商品
        return doMemberOrderReceipt(memberId, orderNo, pointId, null);
    }

    // 用户扫码核销-整单/部分核销(POST):
    // goodsList 为空 = 整单核销(等同 GET /group/order/receipt, 核销全部剩余可核销商品);
    // goodsList 非空 = 部分核销(仅核销所选商品行, 一单可多次部分核销, 每行核销数量不超过"剩余可核销数(购买数-已核销-已退)");
    // 核销成功后统一落核销记录表(核销类型:2=用户扫码核销, 核销人=下单用户, 核销商品明细记录本次核销数量)
    @PostMapping("/group/order/part/receipt")
    public JsonResult orderPartReceiptVerify(@RequestBody MemberOrderReceiptRequest request) {
        log.info("[用户扫码核销(整单/部分核销)] params request:{}", JSON.toJSONString(request));
        if (StringUtils.isEmpty(request.getOrderNo())) {
            throw new BusinessException("订单号不能为空");
        }
        if (request.getPointId() == null || request.getPointId() <= 0) {
            throw new BusinessException("核销自提点不能为空");
        }
        // 查询用户信息
        Long memberId = getLoginMemberId();
        // 部分核销入参: key=订单商品表id, value=本次核销数量请求
        Map<Long, OrderVerifyGoodsRequest> goodsMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(request.getGoodsList())) {
            for (OrderVerifyGoodsRequest temp : request.getGoodsList()) {
                if (temp.getId() == null || temp.getNum() == null || temp.getNum() <= 0) {
                    throw new BusinessException("核销商品id和核销数量必须大于0，请核对后再提交");
                }
                goodsMap.put(temp.getId(), temp);
            }
        }
        // 非空映射=部分核销, 空映射=整单核销
        return doMemberOrderReceipt(memberId, request.getOrderNo(), request.getPointId(),
                goodsMap.isEmpty() ? null : goodsMap);
    }

    // 用户扫码核销统一处理: goodsMap=null 整单核销(核销全部剩余可核销商品), 非空 部分核销(仅核销所选商品行)
    private JsonResult doMemberOrderReceipt(Long memberId, String orderNo, Long pointId, Map<Long, OrderVerifyGoodsRequest> goodsMap) {

        boolean partVerify = goodsMap != null && !goodsMap.isEmpty();
        // 先查询订单信息
        GbOrderInfo orderInfo = orderInfoService.getMiniOrderInfo(memberId, orderNo);
        if (ObjectUtils.isEmpty(orderInfo)) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_NOT_EXIST);
        }

        GbOrgLeaderInfo leaderInfo = leaderInfoService.getLeaderInfo(orderInfo.getLeaderId());
        if (ObjectUtils.isEmpty(leaderInfo)) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_INFO_ERROR);
        }

        int status = orderInfo.getStatus().intValue();
        // 支付状态, 未支付不能收货
        if (status == 0) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_UNPAID_NOT_RECEIPT);
        }

        // 退货状态, 申请退货不能收货
        if (status == 3 || status == 4) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_REFUNDED_NOT_RECEIPT);
        }

        // 整单核销不能重复收货(部分核销不校验收货时间, 由商品行"剩余可核销数"约束, 可多次核销)
        if (!partVerify && status == 2 && orderInfo.getReceiptTime() != null && orderInfo.getReceiptTime() > 0) {
            throw new BusinessException(OrderErrorCodeEnum.DUPLICATE_RECEIPT);
        }

        // 核销(实际领取)自提点
        String pointName = "";
        GbOrgPointInfo pointInfo = pointService.getPointInfo(pointId);
        if (pointInfo != null) pointName = pointInfo.getPointName();

        // 商品行核销数量计算与校验
        List<GbOrderGoodsInfo> goodsList = orderInfoService.getOrderGoodsList(orderNo);
        if (CollectionUtils.isEmpty(goodsList)) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_GOODS_NOT_FOUND);
        }
        // 本次核销的商品行数量映射(核销记录用): key=订单商品id, value=本次核销数量
        Map<Long, Integer> verifyNumMap = new HashMap<>();
        for (GbOrderGoodsInfo goods : goodsList) {
            int goodsNum = goods.getGoodsNum() == null ? 0 : goods.getGoodsNum();
            int receiptNum = goods.getReceiptNum() == null ? 0 : goods.getReceiptNum();
            int refundNum = goods.getRefundNum() == null ? 0 : goods.getRefundNum();
            // 剩余可核销数量 = 购买数 - 已核销(收货)数 - 已退(退待收货)数
            int remainNum = goodsNum - receiptNum - refundNum;
            if (remainNum < 0) {
                throw new BusinessException(OrderErrorCodeEnum.VERIFY_NUM_EXCEED);
            }
            // 本次核销数量: 部分核销取入参, 整单核销取剩余全部
            int verifyNum;
            if (partVerify) {
                if (!goodsMap.containsKey(goods.getId())) {
                    // 未选择的商品行不核销
                    continue;
                }
                verifyNum = goodsMap.get(goods.getId()).getNum();
                if (verifyNum > remainNum) {
                    throw new BusinessException(OrderErrorCodeEnum.VERIFY_NUM_EXCEED);
                }
            } else {
                verifyNum = remainNum;
            }
            if (verifyNum > 0) {
                // 记录本次核销数量, 并在内存中累加商品行核销(收货)数量, 供落库同步
                verifyNumMap.put(goods.getId(), verifyNum);
                goods.setReceiptNum(receiptNum + verifyNum);
            }
        }
        if (partVerify && verifyNumMap.isEmpty()) {
            // 部分核销时所选商品行均已无可核销数量
            throw new BusinessException("订单没有可核销的商品数量，请核对后再提交");
        }
        if (!partVerify && verifyNumMap.isEmpty()) {
            // 整单核销时全部商品行均已核销完(如已多次部分核销完)
            throw new BusinessException(OrderErrorCodeEnum.DUPLICATE_RECEIPT);
        }

        // 核销操作: 已有售后状态则核销完成后保持售后(5)不变;
        // 无售后时, 完全核销置已收货(3)+收货时间, 未完全核销置部分收货(2);
        // 均记录核销时间+实际领取自提点, 并同步商品行核销数量
        Boolean flag = orderInfoService.miniVerifyOrder(memberId, orderNo, pointId, pointName, goodsList);
        // 收货消息类型: 1=系统消息2=内部消息3=业务消息
        // 员工-提货点绑定表 gb_org_point_staff 已下线, 不再通知店员, 直接通知团长
        String action = partVerify ? "部分核销" : "主动核销";
        String content = orderInfo.getNickname() + "(" + orderInfo.getMobile() + ")" + action + "了订单号 " + orderInfo.getOrderNo() + " 的订单。";
        messageService.addMiniLeaderMessageInfo(orderInfo.getLeaderId(), 0L, (byte) 3, content);
        // 同步分账订单表, 核销之后的订单才能分账, 这样只要不核销订单, 就可以随时退款
        businessService.updateBusinessOrderCheckStatus(orderNo);
        // 返回结果
        if (flag) {
            // 核销成功: 落核销记录表(核销类型:2=用户扫码核销, 核销人=下单用户);
            // 此时商品行收货数量已含本次核销, 明细按本次核销数量(verifyNumMap)记录
            verifyRecordService.addVerifyRecord(verifyRecordService.buildVerifyRecord(orderInfo, goodsList, verifyNumMap,
                    GbOrderVerifyRecordService.VERIFY_TYPE_MEMBER,
                    memberId, orderInfo.getNickname(), pointId, pointName));
            //订单对应的团长的cashType[结算到账方式,0=支付时延迟到账型,1=核销时延迟到账型]
            int type = leaderInfo.getCashType().intValue();
            // 核销时延迟到账型(1) 且未调用过微信发货的订单, 核销后补调用微信发货(同步发货状态)
            if (type == 1 && !isWxShipmentCalled(orderInfo)) {
                handleCalledWxUploadShippingInfo(orderInfo);
            }
            return JsonResult.success(partVerify ? "部分核销成功" : "收货成功");
        } else {
            throw new BusinessException(OrderErrorCodeEnum.RECEIPT_FAILED);
        }
    }

    // 是否已调用过微信发货(wx_shipment:0=未调用,1=已调用)
    private boolean isWxShipmentCalled(GbOrderInfo orderInfo) {
        Integer wxShipment = orderInfo.getWxShipment();
        return wxShipment != null && wxShipment.intValue() == 1;
    }

    private void handleCalledWxUploadShippingInfo(GbOrderInfo orderInfo) {
        String orderNo = orderInfo.getOrderNo();
        // 微信单号优先取收款账户表, 兜底取支付流水号
        GbOrderBusinessInfo orderBusinessInfo = businessService.getOrderBusinessInfo(orderNo);
        String transactionId = ObjectUtils.isEmpty(orderBusinessInfo) ? null : orderBusinessInfo.getTransactionId();
        if (StringUtils.isEmpty(transactionId)) {
            transactionId = orderInfo.getPayNo();
        }
        if (StringUtils.isEmpty(transactionId)) {
            log.warn("[核销微信发货]未获取到微信单号, orderNo:{}", orderNo);
            return;
        }
        // 微信发货失败不影响核销主流程(核销已完成), 仅记录日志, 由后续自动发货任务兜底重试
        try {
            // 首次调用(使用缓存access_token)
            int wxFlag = callWxUploadShippingInfo(orderNo, transactionId, orderInfo.getGroupName(), orderInfo.getOpenid(), false);
            // access_token失效(微信返回40001/42001, 对应-1)时, 清除缓存并强制刷新后重试一次
            if (wxFlag == -1) {
                wxAccessTokenHelper.removeAccessToken();
                log.warn("[核销微信发货]access_token失效, 清除缓存并强制刷新后重试, orderNo:{}", orderNo);
                wxFlag = callWxUploadShippingInfo(orderNo, transactionId, orderInfo.getGroupName(), orderInfo.getOpenid(), true);
            }
            // 1=微信发货成功
            if (wxFlag == 1) {
                // 标记订单已调用微信发货(wx_shipment:0=未调用,1=已调用)
                orderInfoService.updateWxShipment(orderNo);
                // 同步收款账户订单发货状态(is_send=1, send_time, comm_status=1 已发货), 避免自动发货任务重复发货
                businessService.updateBusinessOrderSendStatus(orderNo);
                log.info("[核销微信发货]核销调用微信发货成功, orderNo:{}", orderNo);
            } else {
                log.error("[核销微信发货]核销调用微信发货失败, orderNo:{}, wxFlag:{}", orderNo, wxFlag);
            }
        } catch (Exception e) {
            log.error("[核销微信发货]核销调用微信发货异常, orderNo:{}", orderNo, e);
        }
    }

    // 调用微信发货信息录入(可强制刷新access_token), 返回: 1=成功, 0=业务失败, -1=access_token失效(40001/42001)
    private int callWxUploadShippingInfo(String orderNo, String transactionId, String groupName, String openid, boolean forceRefreshToken) {
        // forceRefreshToken=true 时忽略Redis缓存, 强制向微信重新获取access_token
        String accessToken = wxAccessTokenHelper.getAccessToken(forceRefreshToken);
        if (StringUtils.isEmpty(accessToken)) {
            log.warn("[核销微信发货]获取access_token失败, orderNo:{}", orderNo);
            return -1;
        }
        return WxMiniProgramHelper.uploadShippingInfo(accessToken, transactionId, groupName, openid);
    }

    // 从请求头token中解析当前登录用户id
    private Long getLoginMemberId() {

        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            throw new BusinessException(OrderErrorCodeEnum.TOKEN_NOT_EXIST);
        }
        String userId = TokenUtils.parseToken(token);
        if (StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        if (memberId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        return memberId;
    }

    // 用户申请订单退款。refundFlag: 1=退款(退"待收货"部分, 可退量=购买数-收货数-已申请退款数), 2=退货退款(退"已收货"部分, 可退量=收货数-已申请退货退款数); 申请成功后: 订单状态置售后(5), 对应商品行退款/退货退款数量先占坑累计(可退量会相应扣减), 商品维度退款金额由 退款数量×商品单价 推算, 不单独落库; 订单主表refund_fee在申请/审核阶段都不维护, 待团长审核: 同意仅按本次申请金额发起退款(主表金额改由退款回调成功后按实际退款金额累加), 不同意=主表金额不变(天然回到申请前), 仅回退商品行本次申请的数量并把售后状态置不同意; 出参data为本次申请退款总金额(单位:元)
    // 事务保证"订单置售后(5) + 商品行退款数量占坑 + 售后申请记录"三步一致, 避免中途异常导致订单已售后但商品行退款数量未维护(待核销统计虚高)
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/group/order/apply/refund")
    public JsonResult orderApplyRefund(@Validated @RequestBody OrderRefundApplyRequest refundApplyRequest) {
        log.info("[用户申请订单退款操作],params->{}", JSON.toJSONString(refundApplyRequest));
        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            throw new BusinessException(OrderErrorCodeEnum.TOKEN_NOT_EXIST);
        }
        String userId = TokenUtils.parseToken(token);
        if (StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        GbMemberInfo memberInfo = memberInfoService.getMemberInfo(memberId);
        if (ObjectUtils.isEmpty(memberInfo)) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        String memberName = memberInfo.getNickname();
        String orderNo = refundApplyRequest.getOrderNo();
        // 先查询订单信息
        GbOrderInfo orderInfo = orderInfoService.getMiniOrderInfo(memberId, orderNo);
        if (ObjectUtils.isEmpty(orderInfo)) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_NOT_EXIST);
        }

        int status = orderInfo.getStatus().intValue();
        // 支付状态, 未支付不能申请团款
        if (status == OrderStatusEnum.UNPAID.getCode()) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_UNPAID_NOT_REFUND);
        }
        // 已经取消状态, 未支付不能收货
        if (status == OrderStatusEnum.CANCELED.getCode()) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_CANCELED_NOT_REFUND);
        }
        // 退货状态, 申请退货不能收货
        if (status == OrderStatusEnum.REFUNDED.getCode()) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_REFUNDED_NOT_REFUND);
        }
        List<GbOrderGoodsInfo> goodsList = orderInfoService.getOrderGoodsList(orderNo);
        log.info("用户申请退款中orderNo:{},goods:{}", orderNo, JSON.toJSONString(goodsList));
        if (CollectionUtils.isEmpty(goodsList)) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_GOODS_NOT_FOUND);
        }

        Map<Long, OrderRefundGoodsRequest> goodsMap = refundApplyRequest.getRefundGoodsMap();
        StringBuilder sb = new StringBuilder();
        // 本次申请退款的总金额(单位:元)
        Double allRefundAmount = 0D;
        //    退款(1): 可退"待收货(尚未收货)"部分 = 购买数量 - 收货数量 - 已申请退款数量
        //    退货退款(2): 可退"已收货"部分 = 收货数量 - 已申请退货退款数量
        int isReturnGoods = Optional.ofNullable(refundApplyRequest.getRefundFlag()).orElse(0).intValue();
        if (isReturnGoods != 1 && isReturnGoods != 2) {
            throw new BusinessException(OrderErrorCodeEnum.REFUND_TYPE_INVALID);
        }
        // 本次申请实际处理的商品行数, 用于校验申请退款的商品都属于该订单
        int handledCount = 0;
        // 收集本次申请的商品行(仅这些行会累加退款/退货退款数量, 避免误加未申请的行)
        List<GbOrderGoodsInfo> applyGoodsList = new ArrayList<>();
        for (GbOrderGoodsInfo orderGoods : goodsList) {
            int applyStatus = orderGoods.getApplyRefund().intValue();
            if (applyStatus == 1) {
                throw new BusinessException(OrderErrorCodeEnum.ORDER_GOODS_NEED_APPROVE);
            }
            Long tempId = orderGoods.getId();
            String tempGoodsName = orderGoods.getGoodsName();
            String spuNames = orderGoods.getSkuNames();
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
            }
            if (isReturnGoods == 1) {
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
                throw new BusinessException("商品名称" + tempGoodsName + "申请退数量必须大于0，请核对后再提交");
            }
            if (canRefundNum <= 0) {
                throw new BusinessException("商品名称" + tempGoodsName + "已无可退数量，请核对后再提交");
            }
            if (applyNum > canRefundNum) {
                throw new BusinessException("商品名称" + tempGoodsName + "申请退数量大于可退数量，请核对后再提交");
            }
            handledCount++;
            // 加入本次申请商品行集合(后续仅这些行累加退款/退货退款数量)
            applyGoodsList.add(orderGoods);
            String decMsg = tempGoodsName + "-" + spuNames + ",申请退数量:" + applyNum + ",退款金额:" + temp.getRefundAmount() + ";";
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
            throw new BusinessException(OrderErrorCodeEnum.REFUND_GOODS_NOT_MATCH);
        }

        String refundGoodsMsg = sb.toString();
        // 累加结果四舍五入精确到分, 避免浮点误差
        allRefundAmount = MoneyUtil.centToYuan(MoneyUtil.yuanToCent(allRefundAmount));
        // 申请退款总金额不能超过订单实付金额(payFee 单位:分), 防止超额退款
        int payFee = orderInfo.getPayFee() == null ? 0 : orderInfo.getPayFee().intValue();
        if (MoneyUtil.yuanToCent(allRefundAmount) > payFee) {
            throw new BusinessException(OrderErrorCodeEnum.REFUND_AMOUNT_EXCEED);
        }

        // 申请退款: 订单状态置为售后(5)待团长审核(主表refund_fee在申请/审核阶段都不累加, 待退款回调成功后才维护)
        Boolean flag = orderInfoService.miniRefundOrder(memberId, orderNo);
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
            // 落库本次申请的类型与金额(单位:分), 团长端审核未回传类型/金额时, 审核处理据此兜底(拒绝回退商品数量/同意按此金额发起退款)
            refundRecord.setRefundFlag(isReturnGoods);
            refundRecord.setRefundAmount(MoneyUtil.yuanToCent(allRefundAmount));
            refundRecord.setActionReason(refundApplyRequest.getActionReason());
            refundRecord.setExtraReason(refundApplyRequest.getExtraReason());
            refundRecord.setAddTime(TimeUtils.getTimeStamp());
            refundRecordService.addRefundRecord(refundRecord);
            // 返回本次申请退款的总金额, 供前端展示
            return JsonResult.success("已申请退款", allRefundAmount);
        } else {
            throw new BusinessException(OrderErrorCodeEnum.REFUND_APPLY_FAILED);
        }

    }

    // 用户退款原因下拉列表(申请退款时"选择退款原因")
    @GetMapping("/group/order/refund/reasonList")
    public JsonResult refundReasonList() {
        log.info("用户退款原因下拉列表接口,开始");
        List<GbRefundReason> reasonList = refundReasonService.getEnabledReasonList();
        return JsonResult.success(reasonList);
    }

    // 售后记录查询
    @GetMapping("/group/order/refund/recodes")
    public JsonResult getRefundRecords(@RequestParam("orderNo") String orderNo) {
        log.info("售后记录查询,orderNo:{}", orderNo);
        // 查询订单信息
        GbOrderInfo orderInfo = orderInfoService.getApplyRefunOrderInfo(orderNo);
        if (ObjectUtils.isEmpty(orderInfo)) {
            return JsonResult.success();
        }
        OrderMainRefundResponse response = new OrderMainRefundResponse(orderInfo);
        List<GbOrderGoodsRefundRecord> recordList = refundRecordService.getRefundRecordListByOrderNo(orderNo);
        log.info("申请售后记录查询orderNo:{},返回recordList:{}", orderNo, JSON.toJSONString(recordList));
        if (!CollectionUtils.isEmpty(recordList)) {
            List<OrderRefundRecordResponse> refundRecordResponses = OrderRefundRecordResponse.getOrderRefundRecordResponseList(recordList);
            response.setRefundRecords(refundRecordResponses);
        }
        log.info("售后记录查询返回resp:{}", JSON.toJSONString(response));
        return JsonResult.success(response);
    }


    //用户端-查询还有商品未全部收货的订单列表(用户id+团长id查询, 状态1待收货/2部分收货/5售后;
    //5售后时商品行还须满足 购买数>(已核销数+退款数); 订单下还存在未核销商品, 返回订单及商品信息)
    @GetMapping("/group/order/notAllReceiptList")
    public JsonResult notAllReceiptOrderList(@RequestParam(value = "shopId") Long shopId) {
        log.info("用户扫码查询所有待核销订单:shopId:{}",shopId);
        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            throw new BusinessException(OrderErrorCodeEnum.TOKEN_NOT_EXIST);
        }
        String userId = TokenUtils.parseToken(token);
        if (StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        if (memberId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        GbOrgShopInfo shopInfo = shopInfoService.getByShopId(shopId);
        if (ObjectUtils.isEmpty(shopInfo)) {
            throw new BusinessException(OrderErrorCodeEnum.SHOP_INFO_UNAVAILABLE);
        }
        Long leaderId = shopInfo.getLeaderId();
        // 查询还有商品未全部收货的订单列表(用户id+团长id, 并回填商品信息)
        List<GbOrderInfo> list = orderInfoService.getNotAllReceiptOrderList(memberId, leaderId);
        log.info("用户扫码查询所有待核销订单orderList:{}",JSON.toJSONString(list));
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
            throw new BusinessException(OrderErrorCodeEnum.TOKEN_NOT_EXIST);
        }
        String userId = TokenUtils.parseToken(token);
        if (StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        if (memberId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
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
            throw new BusinessException(OrderErrorCodeEnum.TOKEN_NOT_EXIST);
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
            if (memberId == 0) {
                throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
            }
        } catch (NumberFormatException e) {
            throw new BusinessException(OrderErrorCodeEnum.USER_NOT_EXIST);
        }

        // 查询订单信息
        RefundOrderInfoResponse result = orderInfoService.getApplyRefundOrderInfo(memberId, request.getOrderNo(), request.getRefundFlag());
        log.info("用户点击申请退货后查询订单信息返回result:{}", JSON.toJSONString(result));
        return JsonResult.success(result);
    }


}
