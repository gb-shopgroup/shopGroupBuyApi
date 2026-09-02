package cn.com.shopgroup.order.controller.leader;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.order.constants.OrderStatusEnum;
import cn.com.shopgroup.order.http.request.LeaderOrderApplyRefundRequest;
import cn.com.shopgroup.order.http.request.LeaderOrderListRequest;
import cn.com.shopgroup.order.http.request.OrderVerifyGoodsRequest;
import cn.com.shopgroup.order.http.request.OrderVerifyRequest;
import cn.com.shopgroup.order.http.request.ScanQRCodeRequest;
import cn.com.shopgroup.order.http.response.LeaderHomeShowDataResponse;
import cn.com.shopgroup.order.http.response.OrderResponse;
import cn.com.shopgroup.order.http.response.OrderStatusResponse;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.service.GbOrderBusinessInfoService;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import cn.com.shopgroup.user.service.GbOrgPointInfoService;
import cn.com.shopgroup.user.service.GbOrgStaffInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import com.alibaba.fastjson2.JSON;
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
    private WxMiniAccessTokenHelper helper;

    // 团长订单列表（按团活动/订单状态/关键字筛选, 关键字支持商品名称或手机号, 分页查询）
    @PostMapping("/leader/order/list")
    public JsonResult leaderOrderList(@RequestBody LeaderOrderListRequest request) {
        log.info("用户端查询订单列表接口-参数request:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        // 请求参数矫正
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize())
                .map(size -> Math.min(size, 100))
                .orElse(10);
        Long groupId = request.getGroupId();
        String keyword = request.getKeyword();
        Integer status = request.getStatus();
        // 查询订单列表
        List<GbOrderInfo> result = orderInfoService.getLeaderOrderList(leaderId, groupId, keyword, status, page, pageSize);
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
            return JsonResult.fail("lid不存在");
        }
        // 请求参数矫正
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize())
                .map(size -> Math.min(size, 100))
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
            return JsonResult.fail("lid不存在");
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
            return JsonResult.fail("lid不存在");
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
            return JsonResult.fail("");
        }
        // 查询订单及其商品列表
        GbOrderInfo info = orderInfoService.getMiniLeaderOrderInfo(leaderId, request.getOrderNo(), request.getReceiptCode());
        if (ObjectUtils.isEmpty(info)) {
            return JsonResult.fail("订单不存在");
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
        if (leaderId == 0) return JsonResult.fail("lid不存在");
        // 查询订单及其商品列表
        GbOrderInfo info = orderInfoService.getMiniLeaderOrderInfo(leaderId, orderNo, "");
        if (ObjectUtils.isEmpty(info)) {
            return JsonResult.fail("订单不存在");
        } else {
            OrderResponse data = new OrderResponse(info);
            return JsonResult.success(data);
        }
    }

    //核销（整单核销）
    @PostMapping("/leader/order/writeOff")
    public JsonResult writeOff(@RequestParam("orderNo") String orderNo, @RequestParam("pid") Long pointId) {
        log.info("[核销订单:/leader/order/writeOff] params orderNo:{},pointId:{}", orderNo, pointId);
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        String opName = "团长本人";
        if (staffId != 0) {
            GbOrgStaffInfo staffInfo = staffService.getStaffInfo(staffId);
            if (ObjectUtils.isEmpty(staffInfo)) {
                return JsonResult.fail("staffId=" + staffId + "未查询到相关员工数据");
            }
            opName = staffInfo.getStaffName();
        }
        // 查看订单是否已经被核销
        GbOrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);
        log.info("订单核销->查询订单信息，orderNo:{},info:{}", orderNo, JSON.toJSONString(orderInfo));
        if (ObjectUtils.isEmpty(orderInfo)) {
            return JsonResult.fail("订单不存在");
        }
        // 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消
        int status = orderInfo.getStatus().intValue();
        if (status == OrderStatusEnum.UNPAID.getCode()) {
            return JsonResult.fail("订单未支付, 不能核销");
        }
        // 退款状态
        if (status == OrderStatusEnum.REFUNDED.getCode()) {
            return JsonResult.fail("订单已退款, 不能核销");
        }
        List<GbOrderGoodsInfo> goodsList = orderInfoService.getOrderGoodsList(orderNo);
        if (CollectionUtils.isEmpty(goodsList)) {
            return JsonResult.fail("该订单未查询到商品信息");
        }
        // 开始核销
        String pointName = "";

        GbOrgPointInfo pointInfo = pointService.getPointInfo(pointId);
        if (ObjectUtils.isEmpty(pointInfo)) {
            return JsonResult.fail("pointId=" + pointId + "未查询到相关point数据");
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
            return JsonResult.success("核销成功");
        } else {
            return JsonResult.success("核销失败");
        }
    }

    // 部分核销订单
    @PostMapping("/leader/order/partWriteOff")
    public JsonResult partWriteOff(@RequestBody OrderVerifyRequest request) {
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        String opName = "团长本人";
        if (staffId != 0) {
            GbOrgStaffInfo staffInfo = staffService.getStaffInfo(staffId);
            if (ObjectUtils.isEmpty(staffInfo)) {
                return JsonResult.fail("staffId=" + staffId + "未查询到相关员工数据");
            }
            opName = staffInfo.getStaffName();
        }
        // 查看订单是否已经被核销
        GbOrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(request.getOrderNo());
        if (ObjectUtils.isEmpty(orderInfo)) {
            return JsonResult.fail("订单不存在");
        }
        // 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消
        int status = orderInfo.getStatus().intValue();
        if (status == OrderStatusEnum.UNPAID.getCode() || status == OrderStatusEnum.CANCELED.getCode()) {
            return JsonResult.fail("订单未支付, 不能核销");
        }
        List<GbOrderGoodsInfo> goodsList = orderInfoService.getOrderGoodsList(request.getOrderNo());
        if (CollectionUtils.isEmpty(goodsList)) {
            return JsonResult.fail("该订单未查询到商品信息");
        }

        // 订单商品收货数量计算
        Map<Long, OrderVerifyGoodsRequest> goodsMap = request.getGoodsMap();
        for (GbOrderGoodsInfo goods : goodsList) {
            //未核销商品数量
            Long tempId = goods.getId();
            Integer tempGoodNum = goods.getGoodsNum(); // 订单商品数量
            Integer tempReceiptNum = goods.getReceiptNum(); // 收货数量
            //未核销商品数量
            int remainNum = tempGoodNum.intValue() - tempReceiptNum.intValue();
            if (remainNum < 0) {
                return JsonResult.fail("核销:商品数量大于订单剩余核销数");
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
                return JsonResult.fail("request.getPid()=" + request.getPid() + "未查询到相关数据");
            }
            pointName = pointInfo.getPointName();
        }
        String receiptCode = orderInfo.getReceiptCode();
        Boolean flag = orderInfoService.receiptMiniLeaderOrder(leaderId, request.getOrderNo(), receiptCode, staffId, opName, request.getPid(), pointName, goodsList);
        // 同步分账订单表, 核销之后的订单才能分账, 这样只要不核销订单, 就可以随时退款
        businessService.updateBusinessOrderCheckStatus(orderInfo.getOrderNo());
        // 返回结果
        if (flag) {
            return JsonResult.success("核销成功");
        } else {
            return JsonResult.success("核销失败");
        }
    }

    // 团长端-查询微信发货
    @GetMapping("/leader/order/send")
    public JsonResult sendOrder(@RequestParam("orderNo") String orderNo) {
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        // 从请求头中获取员工id
//        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
//        if (staffId == 0) {
//            return JsonResult.fail("sid不存在");
//        }
        // 查询订单
        GbOrderBusinessInfo orderInfo = businessService.getOrderBusinessInfo(orderNo);
        if (ObjectUtils.isEmpty(orderInfo)) {
            return JsonResult.fail("orderNo=" + orderNo + ",订单不存在");
        }
        // 是否已经发货
        if (orderInfo.getIsSend() == 1) {
            JsonResult.fail("订单已发货");
        }
        // 再获取访问令牌
        String accessToken = helper.getAccessToken(false);
        // 订单发货参数
        String transactionId = orderInfo.getTransactionId();
        String goodsName = orderInfo.getGroupName();
        String openid = orderInfo.getOpenid();
        // 请求发货
        int isSendOK = WxMiniProgramHelper.uploadShippingInfo(accessToken, transactionId, goodsName, openid);
        if (isSendOK == 1) {
            // 更新发货标识
            businessService.updateBusinessOrderSendStatus(orderNo);
            return JsonResult.success("发货成功");
        } else {
            // 考虑 accessToken 失效问题
            if (isSendOK == -1) helper.removeAccessToken();
            return JsonResult.success("发货失败, 请稍后再试！");
        }
    }

    // 团长控制台head部分-商品总数, 团购, 订单数量, 不考虑提货点
    @GetMapping("/leader/home/show/orders")
    public JsonResult homeShowOrderTotal() {
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        // 从请求头中获取员工id
//        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
//        if (staffId == 0) {
//            return JsonResult.fail("sid不存在");
//        }
        // 返回的数据
        LeaderHomeShowDataResponse response = new LeaderHomeShowDataResponse();
        Integer orderTotal = 0;
        // 订单总金额
        Double amountTotal = 0D;
        // 订单数量
        Double refundAmountTotal = 0D;
        // 订单总数（取消除外）
        List<GbOrderInfo> list = orderInfoService.getAllByLeaderId(leaderId);
        if (!CollectionUtils.isEmpty(list)) {
            orderTotal = list.size();
            amountTotal = list.stream().mapToDouble(GbOrderInfo::getOrderPrice).sum();
            refundAmountTotal = list.stream().mapToDouble(GbOrderInfo::getRefundFee).sum();
        }
        response.setAmountTotal(amountTotal);
        response.setOrderTotal(orderTotal);
        response.setRefundAmountTotal(refundAmountTotal);
        // 返回
        return JsonResult.success(response);
    }

}
