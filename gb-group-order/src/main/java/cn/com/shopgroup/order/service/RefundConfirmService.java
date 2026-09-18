package cn.com.shopgroup.order.service;

import cn.com.shopgroup.order.model.GbOrderInfo;

/**
 * 退款回调成功核心落库服务(事务化)
 * <p>
 * 修复原回调处理中"记退款流水号"与"累加退款金额"分离、非事务的问题:
 * 原逻辑若流水号已写而金额累加前异常, 易宝重试时会被误判"已处理过"导致金额漏记;
 * 现将资金相关的全部落库收敛到同一事务, 全部成功或全部回滚, 异常时由易宝重试整体重做.
 * <p>
 * 幂等基准: 以退款记录表中"该易宝退款单号的系统退款成功记录"判重,
 * 替代原订单表 refund_no 单值比对(一单多次部分退款时单值只记得最后一笔, 存在重复累加风险).
 */
public interface RefundConfirmService {

    /**
     * 退款回调成功核心落库(同一事务内):
     * 1. 幂等判重: 该易宝退款单号已存在"系统退款成功"记录则直接返回 false(不重复落库)
     * 2. 落"系统退款成功"退款记录(带退款单号与实际退款金额, 供与易宝对账)
     * 3. 订单主表 refund_fee 按实际退款金额原子累加
     * 4. 订单表补全退款流水号(最近一笔)与退款时间
     * 5. 退款成功交易流水(流水号直接使用易宝退款单号, 便于对账)
     *
     * @param orderInfo  退款订单(回调解析所得)
     * @param refundNo   易宝退款单号(幂等键)
     * @param refundCent 本次实际退款金额(单位:分)
     * @param amountYuan 本次实际退款金额(单位:元, 仅用于记录展示)
     * @return true=本次首次落库成功; false=该退款单已确认过(幂等跳过)
     */
    boolean confirmRefundSuccess(GbOrderInfo orderInfo, String refundNo, int refundCent, double amountYuan);
}