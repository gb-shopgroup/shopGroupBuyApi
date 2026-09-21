package cn.com.shopgroup.order.controller.leader;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.CustomIdGenerator;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSkuInfoService;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import cn.com.shopgroup.order.constants.PaymentStatusEnum;
import cn.com.shopgroup.order.exception.OrderErrorCodeEnum;
import cn.com.shopgroup.order.http.request.LeaderRefundApplyListRequest;
import cn.com.shopgroup.order.http.request.OrderApproveRequest;
import cn.com.shopgroup.order.http.request.OrderRefundGoodsRequest;
import cn.com.shopgroup.order.http.request.OrderRefundInfoRequest;
import cn.com.shopgroup.order.http.response.LeaderRefundApplyListResponse;
import cn.com.shopgroup.order.http.response.LeaderRefundApplyResponse;
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
import cn.com.shopgroup.yeepay.YeePayUtils;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@Slf4j
@Api(value = "团长端--退款管理")
@RequestMapping("/order")
public class OrderRefundController {

    @Resource
    private GbOrderInfoService orderInfoService;

    @Resource
    private GbOrgStaffInfoService staffService;

    @Resource
    private GbOrgMessageInfoService messageService;
    @Resource
    private GbOrderGoodsRefundRecordService refundRecordService;
    @Resource
    private OrderTransactionLogService transactionLogService;

    @Resource
    private GbGoodsInfoService goodsService;

    @Resource
    private GbGoodsSkuInfoService skuService;

    @Resource
    private GbGroupActivityInfoService groupActivityInfoService;

    @Resource
    private GbOrderBusinessInfoService orderBusinessService;

    @Resource
    private RedisHelper redisHelper;

    // 退款订单数量: 订单中存在商品发生过退款(部分退/整单全退, 审核同意)即计入
    @GetMapping("/leader/refund/count")
    public JsonResult refundOrderCount(@RequestParam("gid") Long groupId, @RequestParam("pid") Long pointId) {
        log.info("退款订单数量 /leader/refund/count groupId:{},pointId:{}", groupId, pointId);
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }

        // 查询总数
        Long total = orderInfoService.getMiniLeaderRefundOrderCount(leaderId, groupId, pointId);
        log.info("退款订单数量返回 leaderId:{},groupId:{},pointId:{},total:{}", leaderId, groupId, pointId, total);
        return JsonResult.success(total);
    }

    // 批量查询用户退款申请数据(团长端): 把用户申请退的数据按订单+商品行结构化展示,
    // 只查"待审核"申请(售后订单 status=5 + 商品行 apply_refund=1),
    // 关键字 keyword(商品名称/手机号) 过滤, 分页返回;
    // 分页与总数共用同一口径(先由 SQL 过滤出"存在待审核商品行"的订单再分页, status 固定为售后5),
    // 每单回填该笔申请的整笔待审核商品行(apply_refund=1, 不按 keyword 过滤商品行, 保证审核不遗漏商品)
    @PostMapping("/leader/refund/applyList")
    public JsonResult refundApplyList(@RequestBody LeaderRefundApplyListRequest request) {
        log.info("团长端-批量查询退款申请数据 /leader/refund/applyList, 参数request:{}", JSON.toJSONString(request));
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

        // 待审核申请订单总数(不受分页影响, 口径与列表一致: 售后订单(5)+商品行apply_refund=1)
        Long total = orderInfoService.getLeaderApplyRefundOrderCount(leaderId, request.getKeyword());
        // 待审核申请订单分页列表: 与总数同一口径(先由 SQL 筛选"存在待审核商品行"的订单再分页), 每单带该笔申请的待审核商品行
        List<GbOrderInfo> orderList = orderInfoService.getLeaderApplyRefundOrderPage(leaderId,
                request.getKeyword(), page, pageSize);
        // 组装: 订单 + 最近一笔申请记录(类型/原因/金额)
        List<LeaderRefundApplyResponse> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orderList)) {
            for (GbOrderInfo order : orderList) {
                GbOrderGoodsRefundRecord applyRecord = pickLatestApplyRecord(order.getOrderNo());
                list.add(LeaderRefundApplyResponse.build(order, applyRecord));
            }
        }
        // 组装返回
        LeaderRefundApplyListResponse response = new LeaderRefundApplyListResponse();
        response.setTotal(total);
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setList(list);
        log.info("团长端-批量查询退款申请数据, leaderId:{}, resp:{}", JSON.toJSONString(response));
        return JsonResult.success(response);
    }

    // 取该订单最近一笔"用户申请"记录: 优先取待审核(is_agree=0)的申请记录;
    // 无待审核记录时(查询已同意/已拒绝历史)按最近一笔带类型/金额的非系统记录兜底,
    // 口径与审核端 resolveRefundFlag / getApplyRefundCent 的兜底逻辑一致, 排除"系统退款成功"回调落库记录
    private GbOrderGoodsRefundRecord pickLatestApplyRecord(String orderNo) {
        List<GbOrderGoodsRefundRecord> recordList = refundRecordService.getRefundRecordListByOrderNo(orderNo);
        GbOrderGoodsRefundRecord fallback = null;
        for (int i = recordList.size() - 1; i >= 0; i--) {
            GbOrderGoodsRefundRecord record = recordList.get(i);
            // 排除退款回调的系统落库记录(非用户申请/人工审核)
            if ("系统退款成功".equals(record.getOperateName())) {
                continue;
            }
            // 最近一笔待审核申请记录
            if (record.getIsAgree() != null && record.getIsAgree().intValue() == 0) {
                return record;
            }
            // 兜底: 最近一笔带类型或金额的记录
            if (fallback == null && (record.getRefundFlag() != null || record.getRefundAmount() != null)) {
                fallback = record;
            }
        }
        log.info("取该订单最近一笔-用户申请-记录data:{}", JSON.toJSONString(fallback));
        return fallback;
    }

    //售后订单审核（同意/不同意）。status: 1=同意, 2=不同意; 每单一行key=订单号, value.refundGoodsMap为本次申请的订单商品行(行内refundNum/refundAmount为本次申请值); 同意=按本次申请金额向易宝发起退款(支持部分退款)、保留申请时占坑的商品数量并把商品售后状态置同意, 同时维护订单主状态: 订单商品全部退完=已退款(4), 否则只要有未退完的=售后(5)(主表退款金额refund_fee不在此累加, 统一由退款回调成功后按实际退款金额累加; 商品维度退款金额以退款数量体现, 可由 退款数量×商品单价 推算); 不同意=主表退款金额不变(申请/审核阶段均未累加, 天然回到申请前), 按行回退商品退款/退货退款数量并把商品售后状态置不同意; 团长端旧版本未回传refundFlag/金额时后端按该订单最近一笔售后记录兜底
    @PostMapping("/leader/refund/approve")
    public JsonResult approveRefundOrder(@Validated @RequestBody OrderApproveRequest approveRequest) {
        log.info("团长管理-审核退款订单处理.../order/refund/approve,参数:{}", JSON.toJSONString(approveRequest));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        Long opId = leaderId;
        String opName = "团长本人";
        if (staffId != 0) {
            GbOrgStaffInfo staffInfo = staffService.getStaffInfo(staffId);
            if (ObjectUtils.isEmpty(staffInfo)) {
                throw new BusinessException("staffId=" + staffId + "未查询到相关员工数据");
            }
            opName = staffInfo.getStaffName();
            opId = staffInfo.getStaffId();
        }
        // 审核结果 1 同意 2 拒绝
        Integer approveStatus = approveRequest.getStatus();
        String reason = approveRequest.getReason();
        //参数设置校验，这里不用判断空map情况
        Map<String, OrderRefundInfoRequest> refundMap = approveRequest.getRefundOrderGoodsMap();
        if (refundMap.isEmpty()) {
            throw new BusinessException(OrderErrorCodeEnum.REFUND_PARAM_ERROR);
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
                throw new BusinessException(OrderErrorCodeEnum.ORDER_GOODS_NOT_FOUND);
            }
            // 订单商品处理: 仅校验本次审核的申请商品, 数量不能超过对应退款类型的剩余可退数量
            /*Map<Long, OrderRefundGoodsRequest> refundGoodsMap = refundInfoRequest.getRefundGoodsMap();
            int isReturnGoods = refundInfoRequest.getRefundFlag() == null ? 0 : refundInfoRequest.getRefundFlag();
            for (GbOrderGoodsInfo goods : goodsList) {
                Long orderGoodsId = goods.getId();
                // 非本次审核的商品行不参与校验
                if (!refundGoodsMap.containsKey(orderGoodsId)) {
                    continue;
                }
                OrderRefundGoodsRequest temp = refundGoodsMap.get(orderGoodsId);
                if (temp.getRefundNum() == null || temp.getRefundNum() <= 0) {
                    throw new BusinessException(OrderErrorCodeEnum.REFUND_QTY_ILLEGAL);
                }
                int goodsNum = goods.getGoodsNum() == null ? 0 : goods.getGoodsNum(); // 订单商品数量
                int receiptNum = goods.getReceiptNum() == null ? 0 : goods.getReceiptNum(); // 收货数量
                // 退款数(退待收货部分, 申请累计)
                int refundNum = goods.getRefundNum() == null ? 0 : goods.getRefundNum();
                // 退货退款数(退已收货部分, 申请累计)
                int refundGoodsNum = goods.getRefundGoodsNum() == null ? 0 : goods.getRefundGoodsNum();
                int remainRefundNum;
                if (isReturnGoods == 1) {
                    // 退款(未收货部分)的剩余可退数量
                    remainRefundNum = goodsNum - receiptNum - refundNum;
                } else if (isReturnGoods == 2) {
                    // 退货退款(已收货部分)的剩余可退数量
                    remainRefundNum = receiptNum - refundGoodsNum;
                } else {
                    // 兼容旧版本客户端(未传退款类型): 按原口径 购买数量-退货退款数量 校验
                    remainRefundNum = goodsNum - refundGoodsNum;
                }
                if (remainRefundNum < 0) {
                    remainRefundNum = 0;
                }
                if (temp.getRefundNum() > remainRefundNum) {
                    log.info("申请退数量大于可退数,remainRefundNum:{},goods:{}",remainRefundNum, JSON.toJSONString(goods));
                    throw new BusinessException(OrderErrorCodeEnum.REFUND_QTY_EXCEEDED);
                }
            }
*/

            //同意处理
            if (approveStatus == 1) {
                int m = handleAgree(leaderId, opId, opName, orderInfo, refundInfoRequest, reason);
                if (m == 0) {
                    continue;
                }
            } else {
                //拒绝
                handleRefuse(leaderId, opId, opName, refundInfoRequest, reason);
            }
        }
        // 返回
        log.info("团长管理-退款审核处理完成 /order/refund/approve, leaderId:{}, 共处理{}笔订单", leaderId, refundMap.size());
        return JsonResult.success("审核成功");
    }

    //拒绝（不同意）处理: 主表退款金额申请阶段未累加, 拒绝后金额天然回到申请前, 只需回退商品行退款/退货退款数量
    public void handleRefuse(Long leaderId, Long opId, String opName, OrderRefundInfoRequest request, String reason) {
        log.info("【团长端-审核拒绝处理】leaderId:{},opName:{},orderNo:{},reason:{}", leaderId, opName, request == null ? null : request.getOrderNo(), reason);
        String orderNo = request.getOrderNo();
        // 拒绝退款(记录操作人/拒绝原因)
        orderInfoService.editMiniLeaderRefundOrder(orderNo, opName, reason);
        //拒绝请求过来的订单商品
        List<Long> orderGoodsIds = new ArrayList<>();
        for (Map.Entry<Long, OrderRefundGoodsRequest> entry : request.getRefundGoodsMap().entrySet()) {
            Long orderGoodsId = entry.getKey();
            orderGoodsIds.add(orderGoodsId);
        }
        //标识订单商品售后状态不同意
        orderInfoService.updateOrderGoodsApplyStatus(orderNo, orderGoodsIds, 3);
        // ---------- 恢复商品行申请前状态(商品行退款/退货退款数量申请时已占坑; 主表refund_fee不在申请时累加, 拒绝后金额天然回到申请前, 无需扣回) ----------
        // 本次申请退款类型优先取审核请求回传值; 旧版本团长端可能未回传, 则按该订单最近一笔售后申请记录兜底
        int isReturnGoods = resolveRefundFlag(orderNo, request);
        // 恢复商品数量: 回退商品行本次申请累计的退款/退货退款数量, 否则占坑导致无法再次申请/数量虚高
        if (isReturnGoods == 1 || isReturnGoods == 2) {
            Map<Long, Integer> refundNumMap = new HashMap<>();
            for (Map.Entry<Long, OrderRefundGoodsRequest> entry : request.getRefundGoodsMap().entrySet()) {
                OrderRefundGoodsRequest req = entry.getValue();
                if (req.getRefundNum() != null && req.getRefundNum() > 0) {
                    refundNumMap.put(entry.getKey(), req.getRefundNum());
                }
            }
            orderInfoService.deductOrderGoodsRefundByOrderNo(refundNumMap, isReturnGoods);
        } else {
            // 类型仍未知(历史脏数据): 商品数量未回退, 需人工核对或团长端升级后重试
            log.warn("审核拒绝恢复: 订单{}未获取到退款类型, 商品行退款数量未回退", orderNo);
        }
        //插入退货记录售后日志
        GbOrderGoodsRefundRecord refundRecord = new GbOrderGoodsRefundRecord();
        refundRecord.setOperateId(opId);
        refundRecord.setOperateName(opName);
        refundRecord.setIsAgree(2);//不同意
        refundRecord.setOrderNo(orderNo);
        if (isReturnGoods == 1 || isReturnGoods == 2) {
            refundRecord.setRefundFlag(isReturnGoods);
        }
        refundRecord.setActionReason(reason);
        refundRecord.setAddTime(TimeUtils.getTimeStamp());
        refundRecordService.addRefundRecord(refundRecord);
        // 同步分账订单表, 核销之后的订单才能分账, 这样只要不核销订单, 就可以随时退款
        orderBusinessService.updateBusinessOrderCheckStatus(orderNo);
        // 同步分账订单表, 核销之后的订单才能分账, 这样只要不核销订单, 就可以随时退款
        //businessService.updateBusinessOrderCheckStatus(request.getOrderNo());
        // 3. 恢复订单主状态: 拒绝并不产生真实退款, 该订单无其它待审核售后时从售后(5)恢复为申请前状态, 否则订单一直卡在售后
        // orderInfoService.restoreOrderStatusAfterRefundReview(request.getOrderNo());

        // 添加日志, 消息类型: 1=系统消息2=内部消息3=业务消息
        Byte type = 2;
        String oper = "拒绝了";
        String content = opName + " " + oper + " 订单号(" + orderNo + ") 的退款订单。";
        messageService.addMiniLeaderMessageInfo(leaderId, opId, type, content);
    }

    //同意退款处理: 按本次申请金额发起退款, 主表refund_fee留待退款回调成功后累加
    //防多退三道防线: 1)订单维度分布式锁拦截重复/并发提交 2)商品行须仍处于待审核(已审核过的重复请求拒绝) 3)累计退款金额不超过订单实付
    public int handleAgree(Long leaderId, Long opId, String opName, GbOrderInfo orderInfo, OrderRefundInfoRequest request, String reason) {
        // 申请退款账户
        String merchantNo = orderInfo.getMerchantNo();
        String orderNo = orderInfo.getOrderNo();
        // 防线1: 订单维度分布式锁, 拦截团长端重复点击/网络重试/并发提交, 防止向易宝重复发起退款(每次refundRequestId均为随机值, 易宝侧视为不同退款单, 会真实多退)
        String lockKey = RedisConstant.RedisRefundApproveKey + orderNo;
        if (!redisHelper.getLock(lockKey, RedisConstant.RedisRefundApproveExpired)) {
            log.warn("退款审核防重: 订单{}存在处理中的退款审核, 本次请求忽略, 操作人:{}", orderNo, opName);
            return 0;
        }
        try {
            // 本次审核同意的退款金额(单位:分): 优先取审核请求回传的本次申请金额, 团长端旧版本未回传时按最近一笔申请记录兜底
            int agreeRefundCent = getApplyRefundCent(orderNo, request);
            // 按本次申请金额发起退款(支持部分退款); 申请金额缺失时退化为整单实付金额, 避免向易宝发起0元退款
            int requestRefundCent = agreeRefundCent > 0 ? agreeRefundCent
                    : (orderInfo.getPayFee() == null ? 0 : orderInfo.getPayFee());
            if (requestRefundCent <= 0) {
                log.warn("退款发起终止: 订单{}本次可退金额为0", orderNo);
                return 0;
            }
            // 查询订单商品(供待审核校验与库存回补共用)
            List<GbOrderGoodsInfo> goodsList = orderInfoService.getOrderGoodsList(orderNo);
            // 防线2: 本次审核的商品行中须仍有待审核(apply_refund=1)的行; 全部已处理过视为重复审核请求, 直接拒绝(防重复发起退款)
            if (!hasPendingRefundGoods(goodsList, request.getRefundGoodsMap())) {
                log.warn("退款审核防重: 订单{}本次审核的商品行均已处理过(apply_refund!=1), 视为重复审核请求, 拒绝再次发起退款, 操作人:{}", orderNo, opName);
                return 0;
            }
            // 防线3: 累计金额防超退: 已同意(已向易宝发起)累计金额 + 本次金额 <= 订单实付金额
            int payFee = orderInfo.getPayFee() == null ? 0 : orderInfo.getPayFee();
            int agreedCent = refundRecordService.getAgreedRefundCentByOrderNo(orderNo);
            if (agreedCent + requestRefundCent > payFee) {
                log.error("退款防超退: 订单{}累计退款金额({}分+本次{}分)超过实付金额{}分, 终止发起退款, 操作人:{}", orderNo, agreedCent, requestRefundCent, payFee, opName);
                return 0;
            }
            // 退款金额, 分转元
            double amount = MoneyUtil.centToYuan(requestRefundCent);
            Map<String, String> res = YeePayUtils.refund(merchantNo, orderNo, String.valueOf(amount));
            // 易宝退款单号(受理成功时返回): 落退款记录供与易宝对账, 并作为退款回调幂等键
            String uniqueRefundNo = res == null || res.get("data") == null ? "" : res.get("data");
            log.info("退款发起: 订单{}, 本次退款{}分, 易宝退款单号:{}, 操作人:{}", orderNo, requestRefundCent, uniqueRefundNo, opName);
            //插入交易流水表
            OrderTransactionLog transactionLog = new OrderTransactionLog();
            transactionLog.setOrderNo(orderNo);
            transactionLog.setTransactionNo("tr" + CustomIdGenerator.generateUUID());
            transactionLog.setPayAmount(amount);
            transactionLog.setRefundAmount(amount);
            transactionLog.setPayMethod("yeePay");
            transactionLog.setOperatorId(opId);
            transactionLog.setOperatorName(opName);
            transactionLog.setAddTime(TimeUtils.getTimeStamp());
            transactionLog.setPayStatus(PaymentStatusEnum.REFUNDED.getCode());
            // 查看是否成功
            if (Integer.parseInt(res.get("success")) == 0) {
                log.error("退款发起失败: 订单{}, 易宝返回:{}", orderNo, res.get("data"));
                transactionLog.setRemark("退款发起失败");
                handleInsertTransaction(transactionLog);
                return 0;
            } else {
                // 注: 主表退款金额refund_fee不在此维护, 申请/审核阶段都不累加;
                //     统一由退款回调成功后按易宝回传的实际退款金额累加(见 RefundConfirmServiceImpl#confirmRefundSuccess)
                // 退款受理成功, 按实际退款数量回补库存(商品总库存 + SKU库存), 支持部分退款
                // 注: 商品行的退款/退货退款数量已在用户申请时累计占坑, 审核同意后保留不再重复累加
                // 回补口径与下单扣减完全对称:
                // 1) 仅当商品 is_stock=1(启用库存管理, 下单时确实扣减了)时才回补; is_stock=0 商品当时未扣减, 不能凭空回补;
                // 2) 数量公式: refundNum × packNum(与下单 reduceNum 一致);
                // 3) 有 SKU 时同步回补 SKU 库存, 保证 sku库存 与 商品总库存 一致;
                // 4) 已下架/已关闭的商品和 SKU 仍可回补(下单时它们可能为在线状态, 已成功扣减; 库存回补不依赖其当前销售状态, 仅依赖下单时的扣减事实)
                Set<Long> stockChangedGoodsIds = new HashSet<>();
                for (GbOrderGoodsInfo goods : goodsList) {
                    OrderRefundGoodsRequest refundGoods = request.getRefundGoodsMap().get(goods.getId());
                    int refundNum = refundGoods != null && refundGoods.getRefundNum() != null ? refundGoods.getRefundNum() : 0;
                    if (refundNum <= 0) {
                        continue;
                    }
                    // 取当前商品基础信息, 用于判断是否实际参与了扣减(is_stock)
                    GbGoodsInfo goodsInfo = goodsService.getGoodsInfo(goods.getGoodsId());
                    if (ObjectUtils.isEmpty(goodsInfo)) {
                        // 商品已被删除, 仍按订单快照回补, 但仅能回补商品总库存(skuId可能已无对应记录); 跳过以避免误回补到错误商品, 改为日志告警
                        log.warn("退款回补库存: 订单{} 商品{}已被删除, 跳过库存回补, 需人工核对", orderNo, goods.getGoodsId());
                        continue;
                    }
                    if (goodsInfo.getIsStock() == null || goodsInfo.getIsStock().intValue() != 1) {
                        // 该商品当前不参与库存管理(可能运营后续关闭了 is_stock); 下单时未扣减, 此处不能凭空回补
                        log.info("退款跳过库存回补(商品当前不参与库存管理): orderNo={}, goodsId={}, isStock={}",
                                orderNo, goods.getGoodsId(), goodsInfo.getIsStock());
                        continue;
                    }
                    int packNum = goods.getPackNum() == null || goods.getPackNum() == 0 ? 1 : goods.getPackNum();
                    int stockNum = refundNum * packNum;
                    boolean goodsIncFlag = goodsService.increaseGoodsStock(goods.getGoodsId(), stockNum);
                    boolean skuIncFlag = true;
                    if (goods.getSkuId() != null && goods.getSkuId() > 0) {
                        skuIncFlag = skuService.increaseGoodsStock(goods.getSkuId(), stockNum);
                    }
                    log.info("退款回补库存: orderNo={}, goodsId={}, skuId={}, refundNum={}, packNum={}, stockNum={}, goodsInc={}, skuInc={}",
                            orderNo, goods.getGoodsId(), goods.getSkuId(), refundNum, packNum, stockNum, goodsIncFlag, skuIncFlag);
                    stockChangedGoodsIds.add(goods.getGoodsId());
                }
                // 退款成功后清理团购商品列表缓存(C端 /goods/group/goods/list)与团购详情缓存(影响 C端 团购详情价格/库存展示)
                // 同时清理该团购的订单数计数器(团购详情页/跟团列表暴露, 退款成功后需刷新)
                if (!stockChangedGoodsIds.isEmpty()) {
                    Set<Long> stockChangedGroupIds = new HashSet<>();
                    for (Long gid : stockChangedGoodsIds) {
                        List<Long> groupIds = groupActivityInfoService.getGroupIdsByGoodsId(gid);
                        if (groupIds != null) {
                            stockChangedGroupIds.addAll(groupIds);
                        }
                    }
                    for (Long groupId : stockChangedGroupIds) {
                        redisHelper.deleteObject(RedisConstant.RedisGroupInfoKey + groupId);
                        redisHelper.deleteObject(RedisConstant.RedisGroupGoodsListKey + groupId);
                        redisHelper.deleteObject(RedisConstant.RedisOrderTotalKey + groupId);
                    }
                }
                //本次审核同意的商品行id集合
                List<Long> orderGoodsIds = new ArrayList<>();
                for (Map.Entry<Long, OrderRefundGoodsRequest> entry : request.getRefundGoodsMap().entrySet()) {
                    Long orderGoodsId = entry.getKey();
                    orderGoodsIds.add(orderGoodsId);
                }
                //标识订单商品售后状态同意
                orderInfoService.updateOrderGoodsApplyStatus(orderNo, orderGoodsIds, 2);
                //插入退货记录售后日志: 补齐本次退款金额/类型/易宝退款单号(旧逻辑未落这些字段, 导致无法与易宝对账、无法防超退校验)
                GbOrderGoodsRefundRecord refundRecord = new GbOrderGoodsRefundRecord();
                refundRecord.setOperateId(opId);
                refundRecord.setOperateName(opName);
                refundRecord.setIsAgree(1);//同意退款
                refundRecord.setOrderNo(orderNo);
                int refundFlag = resolveRefundFlag(orderNo, request);
                if (refundFlag == 1 || refundFlag == 2) {
                    refundRecord.setRefundFlag(refundFlag);
                }
                refundRecord.setRefundAmount(requestRefundCent);
                refundRecord.setRefundNo(uniqueRefundNo);
                refundRecord.setActionReason(reason);
                refundRecord.setAddTime(TimeUtils.getTimeStamp());
                refundRecordService.addRefundRecord(refundRecord);
                // 审核同意且退款受理成功后维护订单主状态: 订单商品全部退完 -> 已退款(4), 否则只要有未退完的 -> 售后(5)
                orderInfoService.updateOrderStatusAfterRefundAgree(orderNo);
                // 添加日志, 消息类型: 1=系统消息2=内部消息3=业务消息
                byte type = 2;
                String opString = "通过了";
                String content = opName + " " + opString + " 订单号(" + orderNo + ") 的退款订单。";
                messageService.addMiniLeaderMessageInfo(leaderId, opId, type, content);
                // 返回 成功标识
                transactionLog.setRemark("退款已受理,易宝退款单号:" + uniqueRefundNo);
                handleInsertTransaction(transactionLog);
                log.info("退款审核同意处理完成: 订单{}, 本次退款{}分, 易宝退款单号:{}, 操作人:{}", orderNo, requestRefundCent, uniqueRefundNo, opName);
                return 1;
            }
        } finally {
            // 处理完成释放锁(60秒兜底过期), 不影响该订单后续正常的部分退款审核
            redisHelper.releaseLock(lockKey);
        }
    }

    // 本次审核的商品行中是否仍有待审核(apply_refund=1)的行(防重复审核: 全部已处理过说明是重复请求)
    private boolean hasPendingRefundGoods(List<GbOrderGoodsInfo> goodsList, Map<Long, OrderRefundGoodsRequest> refundGoodsMap) {
        if (CollectionUtils.isEmpty(goodsList) || CollectionUtils.isEmpty(refundGoodsMap)) {
            return false;
        }
        for (GbOrderGoodsInfo goods : goodsList) {
            if (refundGoodsMap.containsKey(goods.getId())
                    && goods.getApplyRefund() != null
                    && goods.getApplyRefund().intValue() == 1) {
                return true;
            }
        }
        return false;
    }

    // 解析本次退款类型: 优先取审核请求回传值(refundFlag), 旧版本团长端未回传时按该订单最近一笔带类型的售后记录兜底; 仍未知返回0
    private int resolveRefundFlag(String orderNo, OrderRefundInfoRequest request) {
        int flag = request.getRefundFlag() == null ? 0 : request.getRefundFlag();
        if (flag != 1 && flag != 2) {
            List<GbOrderGoodsRefundRecord> recordList = refundRecordService.getRefundRecordListByOrderNo(orderNo);
            for (int i = recordList.size() - 1; i >= 0; i--) {
                GbOrderGoodsRefundRecord record = recordList.get(i);
                if (record.getRefundFlag() != null
                        && (record.getRefundFlag() == 1 || record.getRefundFlag() == 2)) {
                    return record.getRefundFlag();
                }
            }
        }
        return flag;
    }

    // 计算本次审核(同意)对应的申请退款金额(单位:分): 优先取审核请求回传的本次申请金额;
    // 团长端旧版本未回传金额时, 按该订单最近一笔带金额的售后申请记录兜底
    private int getApplyRefundCent(String orderNo, OrderRefundInfoRequest request) {
        int refundCent = 0;
        if (request != null && !CollectionUtils.isEmpty(request.getRefundGoodsMap())) {
            for (OrderRefundGoodsRequest req : request.getRefundGoodsMap().values()) {
                if (req.getRefundAmount() != null && req.getRefundAmount() > 0) {
                    refundCent += MoneyUtil.yuanToCent(req.getRefundAmount());
                }
            }
        }
        if (refundCent <= 0) {
            List<GbOrderGoodsRefundRecord> recordList = refundRecordService.getRefundRecordListByOrderNo(orderNo);
            for (int i = recordList.size() - 1; i >= 0; i--) {
                GbOrderGoodsRefundRecord record = recordList.get(i);
                if (record.getRefundAmount() != null && record.getRefundAmount() > 0) {
                    refundCent = record.getRefundAmount();
                    break;
                }
            }
        }
        return refundCent;
    }

    private void handleInsertTransaction(OrderTransactionLog transactionLog) {
        transactionLogService.addTransactionLog(transactionLog);
    }

}
