package cn.com.shopgroup.order.service;

import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.order.model.GbOrderCommissionInfo;
import java.util.List;

public interface GbOrderCommissionInfoService {

    /**
     * 分账成功后维护分账信息明细(gb_order_commission_info), 与易宝分账请求明细一一对应:
     * 1) 平台抽成: comm_type=2, comm_fee=平台服务费(service_fee)
     * 2) 商户货款: comm_type=1, comm_fee=商户分账金额(bus_fee), 分账方为收款账户
     * 幂等: 以 订单号+分账方类型 为准, 已存在则更新分账金额/流水号, 避免定时任务重复执行写入重复明细
     * 注: 本方法吞掉异常并返回false, 避免分账信息落库失败中断定时任务整批分账
     *
     * @param orderInfo       订单收款账户信息(携带订单金额、平台服务费、商户分账金额、收款账户)
     * @param uniqueDivideNo  易宝分账流水号
     * @return 是否全部维护成功
     */
    boolean saveDivideCommissionInfo(GbOrderBusinessInfo orderInfo, String uniqueDivideNo);

    // 查询订单下某个分账方类型的分账明细
    GbOrderCommissionInfo getCommissionInfo(String orderNo, Byte commType);

    // 查询订单下全部分账明细
    List<GbOrderCommissionInfo> getCommissionInfoList(String orderNo);

}