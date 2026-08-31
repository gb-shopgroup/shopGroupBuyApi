package cn.com.shopgroup.order.service;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.order.mapper.GbOrderBusinessInfoMapper;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbOrderBusinessInfoService {

    @Resource
    private GbOrderBusinessInfoMapper mapper;


    public List<GbOrderBusinessInfo> getAdminOrderBusinessList(int page, int pageSize) {

        LambdaQueryWrapper<GbOrderBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.orderByDesc(GbOrderBusinessInfo::getId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbOrderBusinessInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public long getAdminOrderBusinessCount() {

        LambdaQueryWrapper<GbOrderBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        return mapper.selectCount(queryWrapper);
    }


    public GbOrderBusinessInfo getOrderBusinessInfo(String orderNo) {

        LambdaQueryWrapper<GbOrderBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderBusinessInfo::getOrderNo, orderNo);
        queryWrapper.orderByAsc(GbOrderBusinessInfo::getId);
        return mapper.selectOne(queryWrapper);
        //return mapper.selectById(orderId);
    }


    public List<GbOrderBusinessInfo> getMiniLeaderOrderBusinessList(Long leaderId, Long busId, int page, int pageSize) {

        LambdaQueryWrapper<GbOrderBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderBusinessInfo::getLeaderId, leaderId);
        queryWrapper.eq(GbOrderBusinessInfo::getBusId, busId);
        queryWrapper.eq(GbOrderBusinessInfo::getIsDivide, 1);
        queryWrapper.eq(GbOrderBusinessInfo::getCommStatus, 3);
        queryWrapper.orderByDesc(GbOrderBusinessInfo::getId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbOrderBusinessInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Long getMiniLeaderOrderBusinessCount(int leaderId, int busId) {

        LambdaQueryWrapper<GbOrderBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderBusinessInfo::getLeaderId, leaderId);
        queryWrapper.eq(GbOrderBusinessInfo::getBusId, busId);
        queryWrapper.eq(GbOrderBusinessInfo::getIsDivide, 1);
        queryWrapper.eq(GbOrderBusinessInfo::getCommStatus, 3);
        return mapper.selectCount(queryWrapper);
    }


    public Integer addMiniLeaderOrderBusiness(GbOrderBusinessInfo info) {

        GbOrderBusinessInfo data = new GbOrderBusinessInfo();
        data.setOrderNo(info.getOrderNo());
        data.setLeaderId(info.getLeaderId());
        data.setBusId(info.getBusId());
        data.setMerchantNo(info.getMerchantNo());
        data.setGroupName(info.getGroupName());
        data.setOrderSn(info.getOrderSn());
        data.setIsSend((byte) 0);
        data.setTransactionId(info.getTransactionId());
        data.setOpenid(info.getOpenid());
        data.setSendTime(0);
        data.setIsUnfreeze((byte) 0);
        data.setUnfreezeTime(0);
        data.setIsDivide((byte) 0);
        data.setDivideStatus("");
        data.setDivideTime(0);
        data.setDivideNo("");
        data.setOrderFee(info.getOrderFee());
        data.setReceivedFee(info.getReceivedFee());
        data.setBusFee(info.getBusFee());
        data.setServiceFee(info.getServiceFee());
        data.setOtherFee(info.getOtherFee());
        data.setCommStatus((byte) 0);
        data.setAddTime(info.getAddTime());
        Integer flag = mapper.insert(data);
        return flag;
    }


    public int editMiniLeaderOrderBusinessRefundStatus(String orderNo) {

        LambdaUpdateWrapper<GbOrderBusinessInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderBusinessInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderBusinessInfo::getCommStatus, 5);
        int flag = mapper.update(updateWrapper);
        return flag;
    }


    public Boolean updateBusinessOrderSendStatus(String orderNo) {

        LambdaUpdateWrapper<GbOrderBusinessInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderBusinessInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderBusinessInfo::getIsSend, 1);
        updateWrapper.set(GbOrderBusinessInfo::getSendTime, TimeUtils.getTimeStamp());
        updateWrapper.set(GbOrderBusinessInfo::getCommStatus, 1);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean updateBusinessOrderCheckStatus(String orderNo) {

        LambdaUpdateWrapper<GbOrderBusinessInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderBusinessInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderBusinessInfo::getIsUnfreeze, 1);
        updateWrapper.set(GbOrderBusinessInfo::getUnfreezeTime, TimeUtils.getTimeStamp());
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


}
