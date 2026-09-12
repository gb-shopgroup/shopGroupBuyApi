package cn.com.shopgroup.order.service;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.order.mapper.GbOrderCommissionInfoMapper;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.order.model.GbOrderCommissionInfo;
import cn.com.shopgroup.user.mapper.GbOrgBusinessInfoMapper;
import cn.com.shopgroup.user.model.GbOrgBusinessInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GbOrderCommissionInfoService {

    // 分账方类型: 1=商户货款 2=平台抽成 3=分账佣金 4=帮卖佣金
    private static final byte COMM_TYPE_BUSINESS = 1;
    private static final byte COMM_TYPE_PLATFORM = 2;
    // 分账状态: 0=未分账 1=已分账
    private static final byte COMM_STATUS_DIVIDED = 1;

    @Autowired
    private GbOrderCommissionInfoMapper mapper;

    @Autowired
    private GbOrgBusinessInfoMapper orgBusinessInfoMapper;

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
    public boolean saveDivideCommissionInfo(GbOrderBusinessInfo orderInfo, String uniqueDivideNo) {

        if (orderInfo == null || orderInfo.getOrderNo() == null || orderInfo.getOrderNo().isEmpty()) {
            log.warn("分账信息维护: 订单信息为空, 跳过");
            return false;
        }

        String orderNo = orderInfo.getOrderNo();
        int orderFee = orderInfo.getOrderFee() == null ? 0 : orderInfo.getOrderFee();
        int serviceFee = orderInfo.getServiceFee() == null ? 0 : orderInfo.getServiceFee();
        int busFee = orderInfo.getBusFee() == null ? 0 : orderInfo.getBusFee();
        int now = TimeUtils.getTimeStamp();

        List<GbOrderCommissionInfo> commissionList = new ArrayList<>();

        // 1. 平台抽成(平台服务费)
        if (serviceFee > 0) {
            commissionList.add(buildCommissionInfo(orderNo, COMM_TYPE_PLATFORM, 0L, "平台",
                    orderFee, serviceFee, uniqueDivideNo, "平台服务费", now));
        }

        // 2. 商户货款(用户支付商品订单费用)
        if (busFee > 0) {
            Long busId = orderInfo.getBusId() == null ? 0L : orderInfo.getBusId();
            commissionList.add(buildCommissionInfo(orderNo, COMM_TYPE_BUSINESS, busId, getBusName(busId),
                    orderFee, busFee, uniqueDivideNo, "用户支付商品订单费用", now));
        }

        if (commissionList.isEmpty()) {
            log.warn("分账信息维护: 订单{}无可维护的分账明细, serviceFee={}, busFee={}", orderNo, serviceFee, busFee);
            return true;
        }

        boolean success = true;
        for (GbOrderCommissionInfo item : commissionList) {
            try {
                saveOrUpdate(item);
            } catch (Exception ex) {
                success = false;
                log.error("分账信息维护失败: 订单号 = {}, 分账方类型 = {}, 分账金额 = {}", orderNo, item.getCommType(), item.getCommFee(), ex);
            }
        }

        if (success) {
            log.info("分账信息维护成功: 订单号 = {}, 分账流水号 = {}, 明细数量 = {}", orderNo, uniqueDivideNo, commissionList.size());
        }
        return success;
    }

    // 组装分账明细
    private GbOrderCommissionInfo buildCommissionInfo(String orderNo, byte commType, Long commUser, String commName,
                                                     int orderFee, int commFee, String commNo, String commRemark, int now) {

        GbOrderCommissionInfo info = new GbOrderCommissionInfo();
        info.setOrderNo(orderNo);
        info.setCommType(commType);
        info.setCommUser(commUser);
        info.setCommName(commName == null ? "" : commName);
        info.setOrderFee(orderFee);
        info.setCommFee(commFee);
        info.setCommStatus(COMM_STATUS_DIVIDED); // 已分账
        info.setCommTime(now);                   // 分账时间(请求接口时间)
        info.setCommNo(commNo == null ? "" : commNo);
        info.setCommRemark(commRemark == null ? "" : commRemark);
        info.setAddTime(now);
        return info;
    }

    // 保存或更新: 同一订单同一分账方类型只保留一条
    private void saveOrUpdate(GbOrderCommissionInfo info) {

        GbOrderCommissionInfo exist = getCommissionInfo(info.getOrderNo(), info.getCommType());
        if (exist == null) {
            mapper.insert(info);
            return;
        }

        info.setCommId(exist.getCommId());
        info.setAddTime(exist.getAddTime()); // 添加时间保持首次写入值
        mapper.updateById(info);
    }

    // 查询订单下某个分账方类型的分账明细
    public GbOrderCommissionInfo getCommissionInfo(String orderNo, Byte commType) {

        LambdaQueryWrapper<GbOrderCommissionInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderCommissionInfo::getOrderNo, orderNo);
        queryWrapper.eq(GbOrderCommissionInfo::getCommType, commType);
        queryWrapper.orderByAsc(GbOrderCommissionInfo::getCommId);
        queryWrapper.last("limit 0, 1");
        return mapper.selectOne(queryWrapper);
    }

    // 查询订单下全部分账明细
    public List<GbOrderCommissionInfo> getCommissionInfoList(String orderNo) {

        LambdaQueryWrapper<GbOrderCommissionInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderCommissionInfo::getOrderNo, orderNo);
        queryWrapper.orderByAsc(GbOrderCommissionInfo::getCommId);
        List<GbOrderCommissionInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    // 获取收款账户名称(分账方姓名), 查询异常时降级为空串, 不影响分账信息落库
    private String getBusName(Long busId) {

        if (busId == null || busId <= 0) {
            return "";
        }
        try {
            GbOrgBusinessInfo businessInfo = orgBusinessInfoMapper.selectById(busId);
            if (businessInfo == null || businessInfo.getBusName() == null) {
                return "";
            }
            return businessInfo.getBusName();
        } catch (Exception ex) {
            log.error("分账信息维护: 查询收款账户名称失败, busId = {}", busId, ex);
            return "";
        }
    }

}
