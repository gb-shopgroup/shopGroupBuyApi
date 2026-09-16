package cn.com.shopgroup.order.service;

import cn.com.shopgroup.order.http.request.LeaderBillListRequest;
import cn.com.shopgroup.order.http.response.GroupOrderRecordResponse;
import cn.com.shopgroup.order.http.response.LeaderBillListResponse;
import cn.com.shopgroup.order.http.response.LeaderMemberDetailResponse;
import cn.com.shopgroup.order.http.response.LeaderMemberListResponse;
import cn.com.shopgroup.order.http.response.RefundOrderInfoResponse;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import java.util.List;
import java.util.Map;

public interface GbOrderInfoService {

    List<GbOrderInfo> getAdminOrderList(int page, int pageSize);

    long getAdminOrderCount();

    GbOrderInfo getOrderInfo(Long id);

    List<GbOrderGoodsInfo> getOrderGoodsList(String orderNo);

    Long getMiniOrderSalesCount(Long groupId);

    /**
     * 统计某团购活动的跟团人数（已支付、未取消、未退款的去重用户数）
     */
    Integer countGroupMemberNum(Long groupId);

    Long addMiniOrder(GbOrderInfo orderInfo, List<GbOrderGoodsInfo> goodsInfoList);

    List<GbOrderInfo> getMiniOrderList(Long memberId, Long leaderId, Integer page, Integer pageSize);

    Long getMiniOrderCount(Long memberId, Long leaderId);

    GbOrderInfo getMiniOrderInfo(Long memberId, String orderNo);

    boolean miniBusinessOrder(String orderNo, Long busId, String merchantNo);

    Boolean miniPayOrder(String orderNo, String payNo, Integer payFee);

    Boolean miniReceiptOrder(Long memberId, String orderNo, Long pointId, String pointName);

    // 用户申请退款: 仅把订单置为售后(5)待团长审核并记录申请时间; 主表退费金额refund_fee在申请/审核阶段都不维护(占坑),
    // 待退款回调成功后才由 addMiniOrderRefundFee 按实际退款金额累加到订单主表; 拒绝/退款失败时金额无需回退即回到申请前
    Boolean miniRefundOrder(Long memberId, String orderNo);

    // 退款结果回调: 补全订单的退款流水号(来自退款接口)与退款时间;
    // 已记录相同退款流水号时不重复更新, 保证易宝重复通知时回调落库幂等
    Boolean updateOrderRefundInfo(String orderNo, String refundNo, Integer refundTime);

    // 退款回调成功后, 把本次实际退款金额(单位:分)累计维护到订单表refund_fee;
    // 申请与审核阶段都不调用, 只在退款资金最终成功时调用一次(幂等由调用方 OrderRefundNotifyController 保证)
    Boolean addMiniOrderRefundFee(String orderNo, Integer addRefundFee);

    Long getMiniUnReceiptOrderCount(Long memberId);

    /**
     * 查询用户在某团购活动内某商品的已购买数量(数量×包装数), 用于下单前的限购校验;
     * 限购维度为「用户 + 团购活动 + 商品」, 即同一商品在其他团购活动中的购买记录不计入
     */
    Integer getGroupOrderGoodsNum(Long memberId, Long groupId, Long goodsId);

    List<GbOrderInfo> getMiniLeaderOrderList(Long leaderId, Long groupId, Long pointId, int page, int pageSize);

    Long getMiniLeaderOrderCount(Long leaderId, Long groupId, Long pointId, Integer status);

    /**
     * 退款订单数量统计(团长端 /leader/refund/count):
     * 只要订单中存在商品发生过退款(审核同意)即计入, 不要求整单全部退完:
     * 1) 整单已退款: status=4(含历史老数据商品行未标售后状态的全退单)
     * 2) 仅部分商品退款成功: 订单主状态已恢复流转(1/2/3), 但商品行售后状态 apply_refund=2 保留
     * 注: 不限制 verify_time=0, 已核销后退货退款成功的订单同样计入; 售后待审核(apply_refund=1)/被拒(apply_refund=3)未产生真实退款, 不计入
     */
    Long getMiniLeaderRefundOrderCount(Long leaderId, Long groupId, Long pointId);

    Long getMiniLeaderOrderTotal(Long leaderId, Long groupId, Long pointId, int startTime, int endTime);

    Double getMiniLeaderOrderAmount(Long leaderId, Long groupId, Long pointId, int startTime, int endTime);

    Map<String, Long> getMiniLeaderOrderStatusTotal(Long leaderId, Long groupId, Long pointId);

    GbOrderInfo getMiniLeaderOrderInfo(Long leaderId, String orderNo, String code);

    Boolean receiptMiniLeaderOrder(Long leaderId, String orderNo, String receiptCode, Long staffId, String staffName, Long pointId, String pointName);

    Boolean receiptMiniLeaderOrder(Long leaderId, String orderNo, String receiptCode, Long staffId, String staffName,
                                          Long pointId, String pointName, List<GbOrderGoodsInfo> goodsList);

    Boolean editMiniLeaderRefundOrder(String orderNo);

    Boolean editMiniLeaderRefundOrder(String orderNo, String staff, String reason);

    /**
     * 售后审核同意且退款成功后维护订单主状态:
     * 1) 订单商品全部退完 -> 已退款(4)
     * 2) 只要有商品尚未退完 -> 售后(5)(不再恢复为申请前状态, 剩余商品仍可继续申请售后)
     * 调用时机: 团长审核同意且退款接口同步返回成功, 或易宝退款回调最终成功
     *
     * @return true=本次已置为已退款(4), false=仍有未退完的商品, 订单保持售后(5)
     */
    boolean updateOrderStatusAfterRefundAgree(String orderNo);

    /**
     * 订单仍有未退完的商品时, 保持(或置回)售后(5)状态;
     * 已退款(4)的订单不覆盖, 避免把已全退订单改回售后
     */
    boolean keepOrderStatusApplyRefund(String orderNo);

    /**
     * 售后审核结束(审核拒绝 / 退款失败)后, 把仍停留在售后(5)的订单恢复为正常流转状态:
     * 前提: 订单状态仍为售后(5) 且 该订单已无待审核售后(apply_refund=1)的商品行, 避免打断仍在途的其它售后申请
     * 规则: 按用户确认收货时间(receipt_time)与商品行核销/退款完整度推算申请前状态
     * 1) receipt_time=0(未确认过收货): 恢复待收货(1), 用户可继续核销/确认收货; 已分账核销满7天的由定时任务自动完成
     * 2) receipt_time>0(确认过收货): 每行 核销量(receipt_num)+已退未收货量(refund_num)+已退已收货量(refund_goods_num) >= 购买量
     * => 全部商品已处理完, 恢复已提货(3); 否则恢复部分收货(2), 剩余部分继续核销并由定时任务自动完成
     */
    void restoreOrderStatusAfterRefundReview(String orderNo);

    List<Map<String, Object>> getSummaryOrderList(Long leaderId, Integer startTime, Integer endTime);

    List<Map<String, Object>> getSummaryOrderGoodsList(Long leaderId, Integer startTime, Integer endTime);

    List<Map<String, Object>> getSummaryOrderGoodsListByPoint(Long leaderId, Integer startTime, Integer endTime);

    List<Map<String, Object>> getSummaryOrderGoodsSkuList(Long leaderId, Long goodsId, Integer startTime, Integer endTime);

    List<Map<String, Object>> getSummaryOrderGoodsPackList(Long leaderId, Long goodsId, Integer startTime, Integer endTime);

    List<Map<String, Object>> getSummaryOrderGoodsSkuListByPoint(Long leaderId, Long goodsId, Integer startTime, Integer endTime);

    List<Map<String, Object>> getSummaryOrderGoodsPackListByPoint(Long leaderId, Long goodsId, Integer startTime, Integer endTime);

    List<Map<String, Object>> getSummaryPointOrderGoodsList(Long leaderId, Long pointId, Integer startTime, Integer endTime);

    // (团长端首页)商品统计汇总: 订单商品总件数 + 待核销总件数
    Map<String, Object> getSummaryGoodsTotal(Long leaderId, Long pointId, String keyword);

    // (团长端首页)商品维度统计分页列表
    List<Map<String, Object>> getSummaryGoodsPageList(Long leaderId, Long pointId, String keyword, int offset, int limit);

    List<Map<String, Object>> getSummaryPointOrderGoodsSkuList(Long leaderId, Long pointId, Long goodsId, Integer startTime, Integer endTime);

    List<Map<String, Object>> getSummaryPointOrderGoodsPackList(Long leaderId, Long pointId, Long goodsId, Integer startTime, Integer endTime);

    GbOrderInfo getOrderInfoByOrderNo(String orderNo);

    Boolean existsByOrderNo(String orderNo);

    /**
     * 查询所有订单数
     *
     * @param leaderId
     * @return
     */
    List<GbOrderInfo> getAllByLeaderId(Long leaderId);

    List<GbOrderInfo> getPaidOrderInfoBy(Long memberId, Long shopId);

    // 用户端-查询还有商品未全部收货的订单列表(条件: 用户id, 团长id, 店铺id)
    List<GbOrderInfo> getNotAllReceiptOrderList(Long memberId, Long leaderId, Long shopId);

    // 标记订单已调用微信发货(wx_shipment:0=未调用,1=已调用)
    Boolean updateWxShipment(String orderNo);

    // 根据订单号修改确认收货操作标记(click_confirm_flag:0=未操作,1=已操作)
    Boolean updateClickConfirmFlag(String orderNo, Integer flag);

    int updateGoodsNum(List<GbOrderGoodsInfo> goodsList);

    /**
     * 用户申请退款/退货成功后维护商品行的退款占用:
     * 1) 售后状态置待审核(apply_refund=1)
     * 2) 按退款类型累加退款数量(refund_num, 退待收货部分)或退货退款数量(refund_goods_num, 退已收货部分)
     * 注意: 入参 goodsList 中的 refundNum/refundGoodsNum 存放的是"本次申请值", 由调用方在内存中赋值;
     * 商品维度退款金额不落库, 由 退款数量×商品单价 推算
     *
     * @param goodsList     本次申请的商品行(内存值携带本次申请数量)
     * @param isReturnGoods 1=退款(退待收货部分) 2=退货退款(退已收货部分)
     */
    int updateOrderGoodsRefundByOrderNo(List<GbOrderGoodsInfo> goodsList, int isReturnGoods);

    void updateOrderGoodsApplyStatus(String orderNo, List<Long> orderGoodsIds, int status);

    /**
     * 审核拒绝时回退商品行本次申请累计的退款/退货退款数量
     * 申请时商品行数量占坑(主表refund_fee金额改为退款回调成功后才累加, 不在申请时占坑),
     * 拒绝则按本次申请量回退, 否则占坑导致无法再次申请/数量虚高
     *
     * @param refundNumMap  key=订单商品id, value=本次申请数量(审核拒绝时从请求回传)
     * @param isReturnGoods 1=退款(退待收货部分, 回退refund_num) 2=退货退款(退已收货部分, 回退refund_goods_num)
     */
    int deductOrderGoodsRefundByOrderNo(Map<Long, Integer> refundNumMap, int isReturnGoods);

    /**
     * 审核同意某批退款后判断订单商品是否全部退款完成:
     * 每个商品行必须已同意(apply_refund=2)且 退款数量(refund_num) + 退货退款数量(refund_goods_num) >= 购买数量
     * 0=全部退完, 1=还有未退完/未同意的商品
     */
    int getOrderGoodsStatus(String orderNo);

    //用户订单列表查询
    List<GbOrderInfo> getMemberOrderList(Long memberId, Integer status, String goodsName, int page, int pageSize);

    // 用户售后订单列表: 支持按商品名称模糊过滤; 同一订单不同商品可能处于不同售后(审核)状态
    // (1 待审核 2 同意 3 不同意), 因此按(订单,审核状态)拆分返回, 每种状态一行(该行goods为该状态商品子集)
    List<GbOrderInfo> getMemberApplyRefundOrderList(Long memberId, Integer status, String goodsName, int page, int pageSize);

    List<GbOrderInfo> getLeaderOrderList(Long leaderId, Long groupId, Long pointId, String keyword,
                                                Integer status, int page, int pageSize);

    List<GbOrderInfo> getLeaderApplyRefundOrderList(Long leaderId, Long groupId, Long pointId, String keyword,
                                                           Integer applyStatus, int page, int pageSize);

    Integer getSumOfGroupActivityOrder(Long groupId);

    /**
     * 查询团购活动的真实跟团记录
     *
     * @param groupId 团购活动id
     * @param limit   返回条数，默认20，最大50
     * @return 跟团记录列表
     */
    List<GroupOrderRecordResponse> getGroupOrderRecordList(Long groupId, Integer limit);

    /**
     * 团长端-我的团员列表
     */
    List<LeaderMemberListResponse> getLeaderMemberList(Long leaderId, String keyword, Integer page, Integer pageSize);

    /**
     * 团长端-团员详情（含统计与动态）
     */
    LeaderMemberDetailResponse getLeaderMemberDetail(Long leaderId, Long memberId);

    List<GbOrderInfo> getAllByLeaderIdAndPointId(Long leaderId, Long pointId);

    /**
     * 用户端-申请退款/退货前查询订单与可申请商品(快照)
     * 退款类型标识 refundFlag: 1=退款(仅退款, 商品未收货--仅退一次，数量待收货总数) 2=退货退款(商品已收货后退回)
     * - 退款(1): 只返回商品中"待收货"(购买数量-收货数量>0)的商品行
     * - 退货退款(2): 只返回商品中"已收货"(收货数量>0)的商品行
     * 订单不存在/不属于该用户/没有符合退款类型的商品时返回 null
     */
    RefundOrderInfoResponse getApplyRefundOrderInfo(Long memberId, String orderNo, Integer refundFlag);

    GbOrderInfo getApplyRefunOrderInfo(String orderNo);

    /**
     * 团长端-对账单: 按选择的时间范围统计"有效订单数/订单总金额/退款总金额",
     * 并按统计维度(1=按商品, 2=按订单)返回明细分页列表;
     * 口径: 有效订单=已支付(pay_time>0)且未取消(status<>6), 按下单时间(add_time)归集;
     * 未选择时间范围(开始/结束日期未成对传入)时不执行查询, 直接返回空账单
     */
    LeaderBillListResponse getLeaderBillList(Long leaderId, LeaderBillListRequest request);

}