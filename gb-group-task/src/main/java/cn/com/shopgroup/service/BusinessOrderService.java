package cn.com.shopgroup.service;

import cn.com.shopgroup.order.mapper.GbOrderBusinessInfoMapper;
import cn.com.shopgroup.order.mapper.GbOrderInfoMapper;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.common.utils.TimeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusinessOrderService {

    @Resource
    private GbOrderBusinessInfoMapper mapper;

    @Resource
    private GbOrderInfoMapper orderInfoMapper;

    // 根据id查询商户订单
    public GbOrderBusinessInfo getOrderBusinessInfo(String orderNo){

        LambdaQueryWrapper<GbOrderBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderBusinessInfo::getOrderNo, orderNo);
        queryWrapper.orderByAsc(GbOrderBusinessInfo::getId);
        return mapper.selectOne(queryWrapper);
    }

    // 查询待发货订单, 去掉退款订单
    public List<GbOrderBusinessInfo> getUnSendBusinessOrderList(int limit){

        LambdaQueryWrapper<GbOrderBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderBusinessInfo::getIsSend, 0); // 未发货
        queryWrapper.lt(GbOrderBusinessInfo::getCommStatus, 5); // 未退款
        queryWrapper.orderByAsc(GbOrderBusinessInfo::getOrderNo);
        queryWrapper.last("limit 0, " + limit);
        List<GbOrderBusinessInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    // 同步发货标识(CAS: 仅未发货的订单更新成功, 防止定时/手动重复调用微信发货后重复落库)
    public boolean updateBusinessOrderSendStatus(String orderNo){

        LambdaUpdateWrapper<GbOrderBusinessInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderBusinessInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderBusinessInfo::getIsSend, 1);
        updateWrapper.set(GbOrderBusinessInfo::getSendTime, TimeUtils.getTimeStamp());
        updateWrapper.set(GbOrderBusinessInfo::getCommStatus, 0); // 已发货
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }

    // 查询订单是否被解冻
    public List<GbOrderBusinessInfo> getFreezeBusinessOrderList(int limit){

        LambdaQueryWrapper<GbOrderBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderBusinessInfo::getIsUnfreeze, 0); // 未解冻
        queryWrapper.orderByAsc(GbOrderBusinessInfo::getOrderNo);
        queryWrapper.last("limit 0, " + limit);
        List<GbOrderBusinessInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    // 同步解冻标识(CAS: 仅未解冻的订单更新成功, 防止已解冻/已分账订单被重复回调时把commStatus倒退覆盖)
    public boolean updateBusinessOrderFreezeStatus(String orderNo){

        LambdaUpdateWrapper<GbOrderBusinessInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderBusinessInfo::getOrderNo, orderNo);
        updateWrapper.eq(GbOrderBusinessInfo::getIsUnfreeze, 0); // 未解冻
        updateWrapper.set(GbOrderBusinessInfo::getIsUnfreeze, 1); // 已解冻
        updateWrapper.set(GbOrderBusinessInfo::getUnfreezeTime, TimeUtils.getTimeStamp()); // 解冻时间
        updateWrapper.set(GbOrderBusinessInfo::getCommStatus, 2); // 已解冻
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }

    // 查询待分账订单数量, 与 getUnDivideBusinessOrderList 同口径: 已解冻(核销)未分账且未退款的订单
    public long getUnDivideBusinessOrderCount(){

        LambdaQueryWrapper<GbOrderBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderBusinessInfo::getIsDivide, 0);   // 未分账
        queryWrapper.lt(GbOrderBusinessInfo::getCommStatus, 5); // 未退款
        queryWrapper.eq(GbOrderBusinessInfo::getIsUnfreeze, 1); // 已解冻(已核销)
        return mapper.selectCount(queryWrapper);
    }

    // 查询待分账订单, 前提是已经微信解冻, 去掉退款订单
    public List<GbOrderBusinessInfo> getUnDivideBusinessOrderList(int page, int pageSize){

        LambdaQueryWrapper<GbOrderBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderBusinessInfo::getIsDivide, 0);   // 未分账
        queryWrapper.lt(GbOrderBusinessInfo::getCommStatus, 5); // 未退款
        queryWrapper.eq(GbOrderBusinessInfo::getIsUnfreeze, 1); // 已解冻(改成已核销)
        queryWrapper.orderByAsc(GbOrderBusinessInfo::getOrderNo);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbOrderBusinessInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    // 同步分账标识(CAS: 仅未分账的订单更新成功, 防止定时/手动重复触发对同一订单重复分账)
    public Boolean updateBusinessOrderDivideStatus(String orderNo, String status, String no){

        LambdaUpdateWrapper<GbOrderBusinessInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderBusinessInfo::getOrderNo, orderNo);
        updateWrapper.eq(GbOrderBusinessInfo::getIsDivide, 0); // 未分账
        updateWrapper.set(GbOrderBusinessInfo::getIsDivide, 1); // 已分账
        updateWrapper.set(GbOrderBusinessInfo::getDivideStatus, status); // 分账状态
        updateWrapper.set(GbOrderBusinessInfo::getDivideTime, TimeUtils.getTimeStamp()); // 分账时间
        updateWrapper.set(GbOrderBusinessInfo::getDivideNo, no); // 分账流水号
        updateWrapper.set(GbOrderBusinessInfo::getCommStatus, 3); // 已分账
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }

    // 查询待同步微信收货状态的订单: 近48小时内已调用微信发货(wx_shipment=1)且用户未确认收货(click_confirm_flag=0)的订单
    // 返回订单收款账户信息(含微信单号transactionId), 供定时任务向微信查询收货状态
    public List<GbOrderBusinessInfo> getUnConfirmReceiptBusinessOrderList(int startTime, int limit){

        // 1. 查询已微信发货且用户未确认收货的订单号(近48小时下单)
        LambdaQueryWrapper<GbOrderInfo> orderWrapper = Wrappers.lambdaQuery();
        orderWrapper.select(GbOrderInfo::getOrderNo);
        orderWrapper.eq(GbOrderInfo::getWxShipment, 1);       // 已调用微信发货
        orderWrapper.eq(GbOrderInfo::getClickConfirmFlag, 0); // 未确认收货
        orderWrapper.ge(GbOrderInfo::getAddTime, startTime);  // 近48小时
        orderWrapper.orderByAsc(GbOrderInfo::getId);
        orderWrapper.last("limit 0, " + limit);
        List<GbOrderInfo> orderList = orderInfoMapper.selectList(orderWrapper);
        if (orderList == null || orderList.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> orderNos = orderList.stream().map(GbOrderInfo::getOrderNo).collect(Collectors.toList());

        // 2. 查询订单收款账户信息(拿到微信单号)
        LambdaQueryWrapper<GbOrderBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(GbOrderBusinessInfo::getOrderNo, orderNos);
        List<GbOrderBusinessInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    // 同步订单收货情况: 微信返回收货状态后, 标记订单已确认收货(click_confirm_flag=1)
    // CAS: 仅未确认收货的订单更新成功, 防止定时任务并发/重复触发重复落库
    public boolean updateBusinessOrderReceiptStatus(String orderNo){

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.eq(GbOrderInfo::getClickConfirmFlag, 0); // 未确认收货
        updateWrapper.set(GbOrderInfo::getClickConfirmFlag, 1); // 已确认收货
        updateWrapper.set(GbOrderInfo::getUpdateTime, TimeUtils.getTimeStamp());
        int flag = orderInfoMapper.update(null, updateWrapper);
        return flag > 0 ? true : false;
    }

    // 同步发货状态(定时任务): 微信侧已发货(状态>=2)时补齐本地发货标识
    // CAS: 仅未发货且未退款的订单更新成功, 防止定时/手动重复调用微信发货后重复落库, 以及覆盖已解冻/已分账/已退款状态
    public boolean syncBusinessOrderSendStatus(String orderNo){

        LambdaUpdateWrapper<GbOrderBusinessInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderBusinessInfo::getOrderNo, orderNo);
        updateWrapper.eq(GbOrderBusinessInfo::getIsSend, 0);      // 未发货
        updateWrapper.lt(GbOrderBusinessInfo::getCommStatus, 5);  // 未退款
        updateWrapper.set(GbOrderBusinessInfo::getIsSend, 1);     // 已发货
        updateWrapper.set(GbOrderBusinessInfo::getSendTime, TimeUtils.getTimeStamp()); // 发货时间
        updateWrapper.set(GbOrderBusinessInfo::getCommStatus, 1); // 已发货
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }

    // 同步退款状态(定时任务): 微信侧已退款(状态=5)时同步本地数据状态
    // CAS: 仅未退款的订单更新成功, 保证重复触发时幂等
    public boolean syncBusinessOrderRefundStatus(String orderNo){

        LambdaUpdateWrapper<GbOrderBusinessInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderBusinessInfo::getOrderNo, orderNo);
        updateWrapper.lt(GbOrderBusinessInfo::getCommStatus, 5);  // 未退款
        updateWrapper.set(GbOrderBusinessInfo::getCommStatus, 5); // 已退款
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


}
