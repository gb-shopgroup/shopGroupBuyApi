package cn.com.shopgroup.order.controller.leader;

import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.order.constants.OrderStatusEnum;
import cn.com.shopgroup.order.exception.OrderErrorCodeEnum;
import cn.com.shopgroup.order.http.request.LeaderOrderApplyRefundRequest;
import cn.com.shopgroup.order.http.request.LeaderOrderListRequest;
import cn.com.shopgroup.order.http.request.OrderVerifyGoodsRequest;
import cn.com.shopgroup.order.http.request.OrderVerifyRequest;
import cn.com.shopgroup.order.http.request.ScanQRCodeRequest;
import cn.com.shopgroup.order.http.response.LeaderHomeGoodsSummaryResponse;
import cn.com.shopgroup.order.http.response.LeaderHomeShowDataResponse;
import cn.com.shopgroup.order.http.response.OrderResponse;
import cn.com.shopgroup.order.http.response.OrderStatusResponse;
import cn.com.shopgroup.order.http.response.SummaryOrderGoodsResponse;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.service.GbOrderBusinessInfoService;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.user.model.GbOrgLeaderInfo;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import cn.com.shopgroup.user.service.GbOrgLeaderInfoService;
import cn.com.shopgroup.user.service.GbOrgPointInfoService;
import cn.com.shopgroup.user.service.GbOrgStaffInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {
    @Resource
    private GbOrderInfoService orderInfoService;
    @Resource
    private GbOrgStaffInfoService staffService;
    @Resource
    private GbOrgPointInfoService pointService;
    @Resource
    private GbOrderBusinessInfoService businessService;
    @Resource
    private WxMiniAccessTokenHelper wxAccessTokenHelper;
    @Resource
    private GbOrgLeaderInfoService leaderInfoService;

    // 团长订单列表（按团活动/订单状态/关键字筛选, 关键字支持商品名称或手机号, 分页查询）
    @PostMapping("/leader/order/list")
    public JsonResult leaderOrderList(@RequestBody LeaderOrderListRequest request) {
        log.info("用户端查询订单列表接口-参数request:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 请求参数矫正
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize())
                .map(size -> Math.min(size, 20))
                .orElse(10);
        Long groupId = request.getGroupId();
        String keyword = request.getKeyword();
        Integer status = request.getStatus();
        // 查询订单列表
        List<GbOrderInfo> result = orderInfoService.getLeaderOrderList(leaderId, groupId, request.getPointId(), keyword, status, page, pageSize);
        List<OrderResponse> data = OrderResponse.getOrderResponseList(result);
        return JsonResult.success(data);
    }

    // 团长售后订单列表（按团活动/审核状态/关键字筛选, 关键字支持商品名称或手机号, 分页查询）
    @PostMapping("/leader/apply/refundList")
    public JsonResult leaderApplyRefundList(@RequestBody LeaderOrderApplyRefundRequest request) {
        log.info("团长端查询订单列表-售后列表-参数request:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 请求参数矫正
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize())
                .map(size -> Math.min(size, 20))
                .orElse(10);
        Long groupId = request.getGroupId();
        String keyword = request.getKeyword();
        Integer applyStatus = request.getApplyStatus();
        // 查询订单列表
        List<GbOrderInfo> result = orderInfoService.getLeaderApplyRefundOrderList(leaderId, groupId, keyword, applyStatus, page, pageSize);
        List<OrderResponse> data = OrderResponse.getOrderResponseList(result);
        return JsonResult.success(data);
    }

    // 查询订单总数(待核销), 考虑提货点
    @GetMapping("/leader/order/count")
    public JsonResult orderCount(@RequestParam("gid") Long groupId, @RequestParam("pid") Long pointId) {
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 查询总数
        Long total = orderInfoService.getMiniLeaderOrderCount(leaderId, groupId, pointId, 2);
        return JsonResult.success(total);
    }

    // 查询订单状态数量
    @GetMapping("/leader/order/status")
    public JsonResult orderStatus(@RequestParam("gid") Long groupId, @RequestParam("pid") Long pointId) {
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 查询订单数量
        Map<String, Long> result = orderInfoService.getMiniLeaderOrderStatusTotal(leaderId, groupId, pointId);
        if (result.isEmpty()) {
            return JsonResult.success();
        }
        OrderStatusResponse response = new OrderStatusResponse(result);
        return JsonResult.success(response);
    }

    // 团长扫用户订单码接口
    @PostMapping("/leader/order/scanQRCode")
    public JsonResult scanQRCode(@Validated @RequestBody ScanQRCodeRequest request) {
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 查询订单及其商品列表
        GbOrderInfo info = orderInfoService.getMiniLeaderOrderInfo(leaderId, request.getOrderNo(), request.getReceiptCode());
        if (ObjectUtils.isEmpty(info)) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_NOT_EXIST);
        } else {
            OrderResponse data = new OrderResponse(info);
            return JsonResult.success(data);
        }
    }

    // 根据订单号查询订单
    @GetMapping("/leader/order/query")
    public JsonResult queryOrder(@RequestParam("orderNo") String orderNo) {
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        // 查询订单及其商品列表
        GbOrderInfo info = orderInfoService.getMiniLeaderOrderInfo(leaderId, orderNo, "");
        if (ObjectUtils.isEmpty(info)) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_NOT_EXIST);
        } else {
            OrderResponse data = new OrderResponse(info);
            return JsonResult.success(data);
        }
    }

    //核销（整单核销）
    @PostMapping("/leader/order/writeOff")
    public JsonResult writeOff(@RequestParam("orderNo") String orderNo,
                               @RequestParam("pid") Long pointId) {
        log.info("[核销订单:/leader/order/writeOff] params orderNo:{},pointId:{}", orderNo, pointId);
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        GbOrgLeaderInfo leaderInfo = leaderInfoService.getLeaderInfo(leaderId);
        if (ObjectUtils.isEmpty(leaderInfo)) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_INFO_ERROR);
        }
        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        String opName = "团长本人";
        if (staffId != 0) {
            GbOrgStaffInfo staffInfo = staffService.getStaffInfo(staffId);
            if (ObjectUtils.isEmpty(staffInfo)) {
                throw new BusinessException("staffId=" + staffId + "未查询到相关员工数据");
            }
            opName = staffInfo.getStaffName();
        }
        // 查看订单是否已经被核销
        GbOrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);
        log.info("订单核销->查询订单信息，orderNo:{},info:{}", orderNo, JSON.toJSONString(orderInfo));
        if (ObjectUtils.isEmpty(orderInfo)) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_NOT_EXIST);
        }
        // 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消
        int status = orderInfo.getStatus().intValue();
        if (status == OrderStatusEnum.UNPAID.getCode()) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_UNPAID_NOT_WRITEOFF);
        }
        // 退款状态
        if (status == OrderStatusEnum.REFUNDED.getCode()) {
            throw new BusinessException(OrderErrorCodeEnum.REFUNDED_NOT_WRITEOFF);
        }
        List<GbOrderGoodsInfo> goodsList = orderInfoService.getOrderGoodsList(orderNo);
        if (CollectionUtils.isEmpty(goodsList)) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_GOODS_NOT_FOUND);
        }
        // 开始核销
        String pointName = "";

        GbOrgPointInfo pointInfo = pointService.getPointInfo(pointId);
        if (ObjectUtils.isEmpty(pointInfo)) {
            throw new BusinessException("pointId=" + pointId + "未查询到相关point数据");
        }
        if (pointId > 0 && pointInfo != null) {
            pointName = pointInfo.getPointName();
        }
        String receiptCode = orderInfo.getReceiptCode();
        Boolean flag = orderInfoService.receiptMiniLeaderOrder(leaderId, orderNo, receiptCode, staffId, opName, pointId, pointName);
        // 同步分账订单表, 核销之后的订单才能分账, 这样只要不核销订单, 就可以随时退款
        businessService.updateBusinessOrderCheckStatus(orderNo);
        // 返回结果
        if (flag) {
            //核销成功后。同步订单商品数量全部收货（方便展示同步商品核销数量）
            int m = orderInfoService.updateGoodsNum(goodsList);
            //核销成功后，订单对应的团长的cashType[结算到账方式,0=支付时延迟到账型,1=核销时延迟到账型]
            int type = leaderInfo.getCashType().intValue();
            // 核销时延迟到账型(1) 且未调用过微信发货的订单, 核销后补调用微信发货(同步发货状态)
            if (type == 1 && !isWxShipmentCalled(orderInfo)) {
                handleCalledWxUploadShippingInfo(orderInfo);
            }
            return JsonResult.success("核销成功");
        } else {
            return JsonResult.success("核销失败");
        }
    }

    // 部分核销订单
    @PostMapping("/leader/order/partWriteOff")
    public JsonResult partWriteOff(@RequestBody OrderVerifyRequest request) {
        log.info("[核销订单:/leader/order/partWriteOff] params request:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        GbOrgLeaderInfo leaderInfo = leaderInfoService.getLeaderInfo(leaderId);
        if (ObjectUtils.isEmpty(leaderInfo)) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_INFO_ERROR);
        }
        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        String opName = "团长本人";
        if (staffId != 0) {
            GbOrgStaffInfo staffInfo = staffService.getStaffInfo(staffId);
            if (ObjectUtils.isEmpty(staffInfo)) {
                throw new BusinessException("staffId=" + staffId + "未查询到相关员工数据");
            }
            opName = staffInfo.getStaffName();
        }
        // 查看订单是否已经被核销
        GbOrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(request.getOrderNo());
        if (ObjectUtils.isEmpty(orderInfo)) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_NOT_EXIST);
        }
        // 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消
        int status = orderInfo.getStatus().intValue();
        if (status == OrderStatusEnum.UNPAID.getCode() || status == OrderStatusEnum.CANCELED.getCode()) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_UNPAID_NOT_WRITEOFF);
        }
        List<GbOrderGoodsInfo> goodsList = orderInfoService.getOrderGoodsList(request.getOrderNo());
        if (CollectionUtils.isEmpty(goodsList)) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_GOODS_NOT_FOUND);
        }

        // 订单商品收货数量计算
        Map<Long, OrderVerifyGoodsRequest> goodsMap = request.getGoodsMap();
        for (GbOrderGoodsInfo goods : goodsList) {
            //未核销商品数量
            Long tempId = goods.getId();
            Integer tempGoodNum = goods.getGoodsNum(); // 订单商品数量
            Integer tempReceiptNum = goods.getReceiptNum(); // 收货数量
            //已经退款-支付数量
            int refundNum = goods.getRefundNum() == null ? 0 : goods.getRefundNum().intValue();
            //未核销商品数量
            int remainNum = tempGoodNum.intValue() - tempReceiptNum.intValue() - refundNum;
            if (remainNum < 0) {
                throw new BusinessException(OrderErrorCodeEnum.VERIFY_NUM_EXCEED);
            }
            if (goodsMap.containsKey(tempId)) {
                OrderVerifyGoodsRequest temp = goodsMap.get(tempId);
                goods.setReceiptNum(temp.getNum() + tempReceiptNum);
            }
        }
        // 开始核销
        String pointName = "";
        if (request.getPid() > 0) {
            GbOrgPointInfo pointInfo = pointService.getPointInfo(request.getPid());
            if (ObjectUtils.isEmpty(pointInfo)) {
                throw new BusinessException("request.getPid()=" + request.getPid() + "未查询到相关数据");
            }
            pointName = pointInfo.getPointName();
        }
        String receiptCode = orderInfo.getReceiptCode();
        Boolean flag = orderInfoService.receiptMiniLeaderOrder(leaderId, request.getOrderNo(), receiptCode, staffId, opName, request.getPid(), pointName, goodsList);
        // 同步分账订单表, 核销之后的订单才能分账, 这样只要不核销订单, 就可以随时退款
        businessService.updateBusinessOrderCheckStatus(orderInfo.getOrderNo());
        // 返回结果
        if (flag) {
            //订单对应的团长的cashType[结算到账方式,0=支付时延迟到账型,1=核销时延迟到账型]
            int type = leaderInfo.getCashType().intValue();
            // 核销时延迟到账型(1) 且未调用过微信发货的订单, 核销后补调用微信发货(同步发货状态)
            if (type == 1 && !isWxShipmentCalled(orderInfo)) {
                handleCalledWxUploadShippingInfo(orderInfo);
            }
            return JsonResult.success("核销成功");
        } else {
            return JsonResult.success("核销失败");
        }
    }

    // 是否已调用过微信发货(wx_shipment:0=未调用,1=已调用)
    private boolean isWxShipmentCalled(GbOrderInfo orderInfo) {
        Integer wxShipment = orderInfo.getWxShipment();
        return wxShipment != null && wxShipment.intValue() == 1;
    }

    // 核销时调用微信发货(团长结算到账方式=1 核销时延迟到账型): 核销成功后补调用微信发货
    // 与支付回调/团长手动发货/定时任务口径一致: 发货成功同时同步 微信发货标记(wx_shipment) + 收款账户表发货状态(is_send/comm_status)
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

    // 团长端-查询微信发货
    @GetMapping("/leader/order/send")
    public JsonResult sendOrder(@RequestParam("orderNo") String orderNo) {
        log.info("查询微信发货 orderNo:{}", orderNo);
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 查询订单
        GbOrderBusinessInfo orderInfo = businessService.getOrderBusinessInfo(orderNo);
        if (ObjectUtils.isEmpty(orderInfo)) {
            throw new BusinessException("orderNo=" + orderNo + ",订单不存在");
        }
        // 是否已经发货
        if (orderInfo.getIsSend() == 1) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_SENT);
        }
        // 再获取访问令牌
        String accessToken = wxAccessTokenHelper.getAccessToken(false);
        // 订单发货参数
        String transactionId = orderInfo.getTransactionId();
        String goodsName = orderInfo.getGroupName();
        String openid = orderInfo.getOpenid();
        // 请求发货
        int isSendOK = WxMiniProgramHelper.uploadShippingInfo(accessToken, transactionId, goodsName, openid);
        if (isSendOK == 1) {
            // 更新发货标识
            businessService.updateBusinessOrderSendStatus(orderNo);
            // 标记订单已调用微信发货(wx_shipment:0=未调用,1=已调用)
            orderInfoService.updateWxShipment(orderNo);
            return JsonResult.success("发货成功");
        } else {
            // 考虑 accessToken 失效问题
            if (isSendOK == -1) wxAccessTokenHelper.removeAccessToken();
            return JsonResult.success("发货失败, 请稍后再试！");
        }
    }

    // 团长首页订单汇总(head部分): 返回有效订单总数/订单总金额/退款总金额; pointId 传 0 或不传表示不区分提货点, 传具体值则按提货点过滤
    @GetMapping("/leader/home/show/orders")
    public JsonResult homeShowOrderTotal(@RequestParam("pointId") Long pointId) {
        // 从请求头中获取团长id
        log.info("团长获取订单统计情况/leader/home/show/orders,pointId:{}", pointId);
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 返回的数据
        LeaderHomeShowDataResponse response = new LeaderHomeShowDataResponse();
        Integer orderTotal = 0;
        // 订单总金额
        Double amountTotal = 0D;
        // 订单数量
        Double refundAmountTotal = 0D;
        // 订单总数（取消除外）
        List<GbOrderInfo> list = orderInfoService.getAllByLeaderIdAndPointId(leaderId, pointId);
        if (!CollectionUtils.isEmpty(list)) {
            orderTotal = list.size();
            amountTotal = list.stream().mapToDouble(GbOrderInfo::getPayFee).sum();
            // refundFee 单位:分, 汇总前需转元
            refundAmountTotal = list.stream()
                    .mapToDouble(o -> MoneyUtil.centToYuan(o.getRefundFee())).sum();
        }
        response.setAmountTotal(amountTotal);
        response.setOrderTotal(orderTotal);
        response.setRefundAmountTotal(refundAmountTotal);
        // 返回
        log.info("返回详情response:{}", JSON.toJSONString(response));
        return JsonResult.success(response);
    }

    // 团长端订单-商品统计: 返回商品种类总数/待核销总件数 + 每个商品的件数统计(含已核销/未核销), 支持商品名称搜索与分页
    @GetMapping("/leader/home/order/goodsSummary")
    public JsonResult homeGoodsSummary(@RequestParam(value = "pointId", defaultValue = "0") Long pointId,
                                       @RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                                       @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 请求参数矫正
        int currentPage = Optional.ofNullable(page).orElse(1);
        int size = Optional.ofNullable(pageSize).map(s -> Math.min(s, 20)).orElse(10);
        int offset = (currentPage - 1) * size;

        // 商品种类总数 + 待核销总件数(不受分页影响)
        Map<String, Object> totalMap = orderInfoService.getSummaryGoodsTotal(leaderId, pointId, keyword);
        long goodsTotal = 0l;
        long unVerifyTotal = 0l;
        if (totalMap != null && !totalMap.isEmpty()) {
            goodsTotal = totalMap.get("goods_total") == null ? 0L : ((Number) totalMap.get("goods_total")).longValue();
            unVerifyTotal = totalMap.get("unverify_total") == null ? 0L : ((Number) totalMap.get("unverify_total")).longValue();
        }

        // 商品维度统计列表(分页)
        List<Map<String, Object>> results = orderInfoService.getSummaryGoodsPageList(leaderId, pointId, keyword, offset, size);
        List<SummaryOrderGoodsResponse> itemList = new ArrayList<>();
        if (results != null) {
            for (Map<String, Object> item : results) {
                SummaryOrderGoodsResponse resp = new SummaryOrderGoodsResponse();
                resp.setId(((Number) item.get("goods_id")).longValue());
                resp.setName((String) item.get("goods_name"));
                resp.setUnit((String) item.get("goods_unit"));
                long numTotal = ((Number) item.get("num_total")).longValue();
                long unVerifyNum = ((Number) item.get("unverify_num")).longValue();
                resp.setTotal(numTotal);          // 总件数
                resp.setNum2(unVerifyNum);        // 未核销件数
                resp.setNum1(numTotal - unVerifyNum); // 已核销件数
                itemList.add(resp);
            }
        }
        // 组装返回
        LeaderHomeGoodsSummaryResponse response = new LeaderHomeGoodsSummaryResponse();
        response.setGoodsTotal(goodsTotal);
        response.setUnVerifyTotal(unVerifyTotal);
        response.setPage(currentPage);
        response.setPageSize(size);
        response.setList(itemList);
        // 返回
        return JsonResult.success(response);
    }

    // 根据团购活动id统计订单数（实时统计，团长端有需求时使用）
    @PostMapping("/get/groupActivity/totalOrder")
    public JsonResult getSumOfGroupActivityOrder(@RequestParam("groupId") Long groupId) {
        Integer total = 0;
        if (groupId == null || groupId.intValue() == 0) {
            return JsonResult.success(total);
        }
        total = orderInfoService.getSumOfGroupActivityOrder(groupId);
        return JsonResult.success(total);
    }

}
