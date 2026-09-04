package cn.com.shopgroup.order.service;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.order.constants.OrderStatusEnum;
import cn.com.shopgroup.order.mapper.GbOrderGoodsInfoMapper;
import cn.com.shopgroup.order.mapper.GbOrderInfoMapper;
import cn.com.shopgroup.order.model.GbGroupViewLog;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.user.service.GbOrgMessageInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import cn.com.shopgroup.order.http.response.GroupOrderRecordResponse;
import cn.com.shopgroup.order.http.response.LeaderMemberDetailResponse;
import cn.com.shopgroup.order.http.response.LeaderMemberListResponse;
import cn.com.shopgroup.order.http.response.MemberDynamicGroup;
import cn.com.shopgroup.order.http.response.MemberDynamicItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GbOrderInfoService {

    @Resource
    private GbOrderInfoMapper mapper;

    @Resource
    private GbOrderGoodsInfoMapper goodsMapper;

    @Resource
    private GbGroupViewLogService viewLogService;

    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private GbOrgMessageInfoService orgMessageInfoService;


    public List<GbOrderInfo> getAdminOrderList(int page, int pageSize) {

        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.orderByDesc(GbOrderInfo::getId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbOrderInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public long getAdminOrderCount() {

        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        return mapper.selectCount(queryWrapper);
    }


    public GbOrderInfo getOrderInfo(Long id) {

        return mapper.selectById(id);
    }


    public List<GbOrderGoodsInfo> getOrderGoodsList(String orderNo) {

        LambdaQueryWrapper<GbOrderGoodsInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderGoodsInfo::getOrderNo, orderNo);
        queryWrapper.orderByAsc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> results = goodsMapper.selectList(queryWrapper);
        return results == null ? new ArrayList<>() : results;
    }


    public Long getMiniOrderSalesCount(Long groupId) {

        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GbOrderInfo::getOrderNo);
        queryWrapper.eq(GbOrderInfo::getGroupId, groupId);
        queryWrapper.eq(GbOrderInfo::getStatus, 2);
        queryWrapper.eq(GbOrderInfo::getStatus, 5);
        queryWrapper.eq(GbOrderInfo::getStatus, 6);
        //queryWrapper.eq(GbOrderInfo::getIsRefund, 0);
        return mapper.selectCount(queryWrapper);
    }

    public Long addMiniOrder(GbOrderInfo orderInfo, List<GbOrderGoodsInfo> goodsInfoList) {

        return transactionTemplate.execute(new TransactionCallback<Long>() {

            @Override
            public Long doInTransaction(TransactionStatus status) {

                try {


                    mapper.insert(orderInfo);
                    String orderNo = orderInfo.getOrderNo();


                    for (GbOrderGoodsInfo item : goodsInfoList) {
                        item.setOrderNo(orderNo);
                    }
                    goodsMapper.insert(goodsInfoList);


                    return orderInfo.getId();

                } catch (Exception e) {


                    status.setRollbackOnly();
                    e.printStackTrace();
                    log.error("写入订单报错：" + e.toString());
                    return 0L;
                }
            }
        });
    }


    public List<GbOrderInfo> getMiniOrderList(Long memberId, Long leaderId, Integer page, Integer pageSize) {


        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getMemberId, memberId);
        //if(pointId > 0) queryWrapper.eq(GbOrderInfo::getPointId, pointId);
        if (leaderId > 0) queryWrapper.eq(GbOrderInfo::getLeaderId, leaderId);
        queryWrapper.orderByDesc(GbOrderInfo::getId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbOrderInfo> orderList = mapper.selectList(queryWrapper);
        if (orderList == null || orderList.size() == 0) return new ArrayList<>();


        List<Long> orderIds = new ArrayList<>();
        if (orderList != null && orderList.size() > 0) {
            for (GbOrderInfo item : orderList) {
                orderIds.add(item.getId());
            }
        }


        LambdaQueryWrapper<GbOrderGoodsInfo> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.in(GbOrderGoodsInfo::getId, orderIds);
        queryWrapper2.orderByAsc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(queryWrapper2);


        Map<Long, List<GbOrderGoodsInfo>> goodsMap = new HashMap<>();
        for (GbOrderGoodsInfo item : goodsList) {

            Long tempId = item.getId();
            if (goodsMap.containsKey(tempId)) {
                goodsMap.get(tempId).add(item);
            } else {
                List<GbOrderGoodsInfo> temp = new ArrayList<>();
                temp.add(item);
                goodsMap.put(tempId, temp);
            }
        }


        if (orderList != null && orderList.size() > 0) {
            for (GbOrderInfo item : orderList) {
                Long tempId = item.getId();
                if (goodsMap.containsKey(tempId)) {
                    item.setGoodsInfoList(goodsMap.get(tempId));
                } else {
                    item.setGoodsInfoList(new ArrayList<>());
                }
            }
        }


        return orderList;
    }


    public Long getMiniOrderCount(Long memberId, Long leaderId) {

        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getMemberId, memberId);
        //if(pointId > 0) queryWrapper.eq(GbOrderInfo::getPointId, pointId);
        if (leaderId > 0) queryWrapper.eq(GbOrderInfo::getLeaderId, leaderId);
        return mapper.selectCount(queryWrapper);
    }


    public GbOrderInfo getMiniOrderInfo(Long memberId, String orderNo) {


        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getMemberId, memberId);
        queryWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        GbOrderInfo data = mapper.selectOne(queryWrapper);


        LambdaQueryWrapper<GbOrderGoodsInfo> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.eq(GbOrderGoodsInfo::getOrderNo, orderNo);
        queryWrapper2.orderByAsc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(queryWrapper2);


        if (data != null && goodsList != null) {
            data.setGoodsInfoList(goodsList);
        }


        return data;
    }


    public boolean miniBusinessOrder(String orderNo, Long busId, String merchantNo) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderInfo::getBusId, busId);
        updateWrapper.set(GbOrderInfo::getMerchantNo, merchantNo);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean miniPayOrder(String orderNo, String payNo, Integer payFee) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderInfo::getStatus, 1);//待收货-也就是支付成功
        updateWrapper.set(GbOrderInfo::getPayTime, TimeUtils.getTimeStamp());
        updateWrapper.set(GbOrderInfo::getPayNo, payNo);
        updateWrapper.set(GbOrderInfo::getPayFee, payFee);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean miniReceiptOrder(Long memberId, String orderNo, Long pointId, String pointName) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.eq(GbOrderInfo::getMemberId, memberId);
        updateWrapper.set(GbOrderInfo::getStatus, 2);
        updateWrapper.set(GbOrderInfo::getReceiptTime, TimeUtils.getTimeStamp());

        updateWrapper.set(GbOrderInfo::getPointId2, pointId);
        updateWrapper.set(GbOrderInfo::getPointName2, pointName);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean miniRefundOrder(Long memberId, String orderNo) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.eq(GbOrderInfo::getMemberId, memberId);
        updateWrapper.set(GbOrderInfo::getStatus, OrderStatusEnum.APPLY_REFUND.getCode());
        updateWrapper.set(GbOrderInfo::getRefundTime, TimeUtils.getTimeStamp());
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Long getMiniUnReceiptOrderCount(Long memberId) {

        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getStatus, 1);
        queryWrapper.eq(GbOrderInfo::getMemberId, memberId);
        return mapper.selectCount(queryWrapper);
    }


    public Integer getMiniOrderGoodsLimit(Long memberId, Long goodsId) {


        Integer endTime = TimeUtils.getTimeStamp();
        Integer startTime = endTime - 90 * 24 * 60 * 60;


        List<Map<String, Object>> results = mapper.getOrderGoodsLimit(memberId, goodsId, startTime, endTime);


        Integer orderGoodsNum = 0;
        for (Map<String, Object> item : results) {

            if (item == null || item.get("goods_id") == null) continue;
            long tempGoodsId = (Long) item.get("goods_id");
            long tempGoodsNum = ((BigDecimal) item.get("num_total")).longValue();
            if (tempGoodsId == goodsId) orderGoodsNum = (int) tempGoodsNum;
        }


        return orderGoodsNum;
    }


    public List<GbOrderInfo> getMiniLeaderOrderList(Long leaderId, Long groupId, Long pointId, int page, int pageSize) {

        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getLeaderId, leaderId);
        if (groupId > 0) queryWrapper.eq(GbOrderInfo::getGroupId, groupId);
        if (pointId > 0) queryWrapper.eq(GbOrderInfo::getPointId, pointId);


        queryWrapper.eq(GbOrderInfo::getVerifyTime, 0);

        queryWrapper.orderByDesc(GbOrderInfo::getId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbOrderInfo> result = mapper.selectList(queryWrapper);
        if (result == null || result.size() == 0) return new ArrayList<>();


        List<Long> orderIds = new ArrayList<>();
        for (GbOrderInfo item : result) {
            orderIds.add(item.getId());
        }

        LambdaQueryWrapper<GbOrderGoodsInfo> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.in(GbOrderGoodsInfo::getId, orderIds);
        queryWrapper2.orderByDesc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(queryWrapper2);

        Map<Long, List<GbOrderGoodsInfo>> goodsMap = new HashMap<>();
        for (GbOrderGoodsInfo item : goodsList) {
            Long tempId = item.getId();
            if (goodsMap.containsKey(tempId)) {
                goodsMap.get(tempId).add(item);
            } else {
                List<GbOrderGoodsInfo> tempList = new ArrayList<>();
                tempList.add(item);
                goodsMap.put(tempId, tempList);
            }
        }

        for (GbOrderInfo item : result) {
            Long tempId = item.getId();
            if (goodsMap.containsKey(tempId)) {
                item.setGoodsInfoList(goodsMap.get(tempId));
            } else {
                item.setGoodsInfoList(new ArrayList<>());
            }
        }


        return result == null ? new ArrayList<>() : result;
    }


    public Long getMiniLeaderOrderCount(Long leaderId, Long groupId, Long pointId, Integer status) {

        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getLeaderId, leaderId);
        queryWrapper.eq(GbOrderInfo::getStatus, status);
        if (groupId > 0) queryWrapper.eq(GbOrderInfo::getGroupId, groupId);
        if (pointId > 0) queryWrapper.eq(GbOrderInfo::getPointId, pointId);

        queryWrapper.eq(GbOrderInfo::getVerifyTime, 0);

        return mapper.selectCount(queryWrapper);
    }


    public Long getMiniLeaderOrderTotal(Long leaderId, Long groupId, Long pointId, int startTime, int endTime) {

        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getLeaderId, leaderId);
        queryWrapper.eq(GbOrderInfo::getStatus, 1);
        if (groupId > 0) queryWrapper.eq(GbOrderInfo::getGroupId, groupId);
        if (pointId > 0) queryWrapper.eq(GbOrderInfo::getPointId, pointId);

        if (startTime > 0 && endTime > 0) {
            queryWrapper.between(GbOrderInfo::getAddTime, startTime, endTime);
        }
        return mapper.selectCount(queryWrapper);
    }


    public Double getMiniLeaderOrderAmount(Long leaderId, Long groupId, Long pointId, int startTime, int endTime) {

        QueryWrapper<GbOrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("leader_id", leaderId);
        wrapper.eq("is_pay", 1);
        wrapper.eq("is_refund", 0);
        if (groupId > 0) wrapper.eq("group_id", groupId);
        if (pointId > 0) wrapper.eq("point_id", pointId);

        if (startTime > 0 && endTime > 0) {
            wrapper.between("add_time", startTime, endTime);
        }
        wrapper.select("SUM(order_price) AS total");
        List<Map<String, Object>> list = mapper.selectMaps(wrapper);

        if (list == null || list.size() == 0 || list.get(0) == null) {
            return 0D;
        } else {
            return Double.parseDouble(list.get(0).get("total").toString());
        }
    }


    public Map<String, Long> getMiniLeaderOrderStatusTotal(Long leaderId, Long groupId, Long pointId) {


        Map<String, Long> result = new HashMap<>();


        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getLeaderId, leaderId);
        if (groupId > 0) queryWrapper.eq(GbOrderInfo::getGroupId, groupId);
        if (pointId > 0) queryWrapper.eq(GbOrderInfo::getPointId, pointId);

        result.put("all", 0l);

        result.put("unpay", 0l);


        queryWrapper.eq(GbOrderInfo::getStatus, 1);
        queryWrapper.eq(GbOrderInfo::getVerifyTime, 0);
        long unreceipt = mapper.selectCount(queryWrapper);
        result.put("unreceipt", unreceipt);

        result.put("completed", 0l);


        LambdaQueryWrapper<GbOrderInfo> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.eq(GbOrderInfo::getLeaderId, leaderId);
        if (groupId > 0) queryWrapper2.eq(GbOrderInfo::getGroupId, groupId);
        if (pointId > 0) queryWrapper2.eq(GbOrderInfo::getPointId, pointId);

        queryWrapper2.eq(GbOrderInfo::getStatus, 1);
        queryWrapper2.eq(GbOrderInfo::getStatus, 1);
        long refunded = mapper.selectCount(queryWrapper2);
        result.put("refunded", refunded);


        return result;
    }


    public GbOrderInfo getMiniLeaderOrderInfo(Long leaderId, String orderNo, String code) {


        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getLeaderId, leaderId);
        if (!StringUtils.isEmpty(orderNo))
            queryWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        if (code != null && code.length() > 0)
            queryWrapper.eq(GbOrderInfo::getReceiptCode, code);
        queryWrapper.orderByDesc(GbOrderInfo::getId);
        queryWrapper.last("limit 0, 1");
        GbOrderInfo result = mapper.selectOne(queryWrapper);
        if (result == null || result.getId() == 0) return null;


        LambdaQueryWrapper<GbOrderGoodsInfo> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.eq(GbOrderGoodsInfo::getOrderNo, result.getOrderNo());
        queryWrapper2.orderByAsc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(queryWrapper2);
        if (CollectionUtils.isEmpty(goodsList)) {
            result.setGoodsInfoList(new ArrayList<>());
        } else {
            result.setGoodsInfoList(goodsList);
        }


        return result;
    }


    public Boolean receiptMiniLeaderOrder(Long leaderId, String orderNo, String receiptCode, Long staffId, String staffName, Long pointId, String pointName) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbOrderInfo::getStatus, OrderStatusEnum.RECEIVED.getCode());
        updateWrapper.set(GbOrderInfo::getVerifyTime, TimeUtils.getTimeStamp());
        updateWrapper.set(GbOrderInfo::getStaffId, staffId);
        updateWrapper.set(GbOrderInfo::getStaffName, staffName);
        updateWrapper.set(GbOrderInfo::getPointId2, pointId);
        updateWrapper.set(GbOrderInfo::getPointName2, pointName);
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.eq(GbOrderInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);
        byte type = 2;
        String oper = "核销了";
        String content = staffName + " " + oper + " " + receiptCode + " 的订单。";
        if (flag > 0) {
            orgMessageInfoService.addMiniLeaderMessageInfo(leaderId, staffId, type, content);
        }


        return flag > 0 ? true : false;
    }


    public Boolean receiptMiniLeaderOrder(Long leaderId, String orderNo, String receiptCode, Long staffId, String staffName,
                                          Long pointId, String pointName, List<GbOrderGoodsInfo> goodsList) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();


        updateWrapper.set(GbOrderInfo::getStatus, OrderStatusEnum.PART_RECEIVED.getCode());


        updateWrapper.set(GbOrderInfo::getVerifyTime, TimeUtils.getTimeStamp());
        //updateWrapper.set(GbOrderInfo::getReceiptTime, TimeUtils.getTimeStamp());


        updateWrapper.set(GbOrderInfo::getStaffId, staffId);
        updateWrapper.set(GbOrderInfo::getStaffName, staffName);


        updateWrapper.set(GbOrderInfo::getPointId2, pointId);
        updateWrapper.set(GbOrderInfo::getPointName2, pointName);


        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.eq(GbOrderInfo::getLeaderId, leaderId);


        int flag = mapper.update(updateWrapper);


        for (GbOrderGoodsInfo item : goodsList) {
            LambdaUpdateWrapper<GbOrderGoodsInfo> updateGoodsWrapper = Wrappers.lambdaUpdate();
            updateGoodsWrapper.eq(GbOrderGoodsInfo::getOrderNo, orderNo);
            updateGoodsWrapper.eq(GbOrderGoodsInfo::getId, item.getId());
            updateGoodsWrapper.set(GbOrderGoodsInfo::getReceiptNum, item.getReceiptNum());
            goodsMapper.update(updateGoodsWrapper);
        }


        byte type = 2;
        String oper = "核销了";
        String content = staffName + " " + oper + " " + receiptCode + " 的订单。";
        orgMessageInfoService.addMiniLeaderMessageInfo(leaderId, staffId, type, content);


        return flag > 0 ? true : false;
    }


    public Boolean editMiniLeaderRefundOrder(String orderNo) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderInfo::getStatus, 4); // 已退款
        //updateWrapper.set(GbOrderInfo::getRefundTime, TimeUtils.getTimeStamp());
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean editMiniLeaderRefundOrder(String orderNo, String staff, String reason) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        //这里先不确定订单状态处理完再改
        //updateWrapper.set(GbOrderInfo::getStatus, 4);
        updateWrapper.set(GbOrderInfo::getRefundStaff, staff);
        updateWrapper.set(GbOrderInfo::getRefundReason, reason);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    /******************************************************************************************************************/


    public List<Map<String, Object>> getSummaryOrderList(Long leaderId, Integer startTime, Integer endTime) {

        return mapper.getSummaryOrderList(leaderId, startTime, endTime);
    }


    public List<Map<String, Object>> getSummaryOrderGoodsList(Long leaderId, Integer startTime, Integer endTime) {

        return mapper.getSummaryOrderGoodsList(leaderId, startTime, endTime);
    }


    public List<Map<String, Object>> getSummaryOrderGoodsListByPoint(Long leaderId, Integer startTime, Integer endTime) {

        return mapper.getSummaryOrderGoodsListByPoint(leaderId, startTime, endTime);
    }


    public List<Map<String, Object>> getSummaryOrderGoodsSkuList(Long leaderId, Long goodsId, Integer startTime, Integer endTime) {

        return mapper.getSummaryOrderGoodsSkuList(leaderId, goodsId, startTime, endTime);
    }


    public List<Map<String, Object>> getSummaryOrderGoodsPackList(Long leaderId, Long goodsId, Integer startTime, Integer endTime) {

        return mapper.getSummaryOrderGoodsPackList(leaderId, goodsId, startTime, endTime);
    }


    public List<Map<String, Object>> getSummaryOrderGoodsSkuListByPoint(Long leaderId, Long goodsId, Integer startTime, Integer endTime) {

        return mapper.getSummaryOrderGoodsSkuListByPoint(leaderId, goodsId, startTime, endTime);
    }


    public List<Map<String, Object>> getSummaryOrderGoodsPackListByPoint(Long leaderId, Long goodsId, Integer startTime, Integer endTime) {

        return mapper.getSummaryOrderGoodsPackListByPoint(leaderId, goodsId, startTime, endTime);
    }

    public List<Map<String, Object>> getSummaryPointOrderGoodsList(Long leaderId, Long pointId, Integer startTime, Integer endTime) {

        return mapper.getSummaryPointOrderGoodsList(leaderId, pointId, startTime, endTime);
    }


    public List<Map<String, Object>> getSummaryPointOrderGoodsSkuList(Long leaderId, Long pointId, Long goodsId, Integer startTime, Integer endTime) {

        return mapper.getSummaryPointOrderGoodsSkuList(leaderId, pointId, goodsId, startTime, endTime);
    }


    public List<Map<String, Object>> getSummaryPointOrderGoodsPackList(Long leaderId, Long pointId, Long goodsId, Integer startTime, Integer endTime) {

        return mapper.getSummaryPointOrderGoodsPackList(leaderId, pointId, goodsId, startTime, endTime);
    }


    public GbOrderInfo getOrderInfoByOrderNo(String orderNo) {
        return mapper.getOrderInfoByOrderNo(orderNo);
    }

    public Boolean existsByOrderNo(String orderNo) {
        int count = mapper.existsByOrderNo(orderNo);
        if (count > 0) {
            return true;
        }
        return false;
    }

    /**
     * 查询所有订单数
     *
     * @param leaderId
     * @return
     */
    public List<GbOrderInfo> getAllByLeaderId(Long leaderId) {
        return mapper.getAllByLeaderId(leaderId);
    }

    public List<GbOrderInfo> getPaidOrderInfoBy(Long memberId, Long shopId) {
        return mapper.getPaidOrderInfoBy(memberId, shopId);
    }

    // 用户端-查询还有商品未全部收货的订单列表(条件: 用户id, 团长id, 店铺id)
    public List<GbOrderInfo> getNotAllReceiptOrderList(Long memberId, Long leaderId, Long shopId) {
        return mapper.getNotAllReceiptOrderList(memberId, leaderId, shopId);
    }

    // 标记订单已调用微信发货(wx_shipment:0=未调用,1=已调用)
    public Boolean updateWxShipment(String orderNo) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderInfo::getWxShipment, 1);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }

    // 根据订单号修改确认收货操作标记(click_confirm_flag:0=未操作,1=已操作)
    public Boolean updateClickConfirmFlag(String orderNo, Integer flag) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderInfo::getClickConfirmFlag, flag);
        int result = mapper.update(updateWrapper);
        return result > 0 ? true : false;
    }

    public int updateGoodsNum(List<GbOrderGoodsInfo> goodsList) {
        if (CollectionUtils.isEmpty(goodsList)) {
            return 0;
        }
        for (GbOrderGoodsInfo item : goodsList) {
            LambdaUpdateWrapper<GbOrderGoodsInfo> updateGoodsWrapper = Wrappers.lambdaUpdate();
            updateGoodsWrapper.eq(GbOrderGoodsInfo::getId, item.getId());
            updateGoodsWrapper.set(GbOrderGoodsInfo::getReceiptNum, item.getGoodsNum());
            goodsMapper.update(updateGoodsWrapper);
        }
        //后续需要逻辑再改成具体条数
        return 1;
    }

    public int updateOrderGoodsRefundByOrderNo(List<GbOrderGoodsInfo> goodsList) {
        if (CollectionUtils.isEmpty(goodsList)) {
            return 0;
        }
        for (GbOrderGoodsInfo item : goodsList) {
            LambdaUpdateWrapper<GbOrderGoodsInfo> updateGoodsWrapper = Wrappers.lambdaUpdate();
            updateGoodsWrapper.eq(GbOrderGoodsInfo::getId, item.getId());
            updateGoodsWrapper.set(GbOrderGoodsInfo::getApplyRefund, item.getApplyRefund());
            updateGoodsWrapper.setSql("refund_goods_num = refund_goods_num + {0}", Math.abs(item.getRefundGoodsNum()));
            goodsMapper.update(updateGoodsWrapper);
        }
        //后续需要逻辑再改成具体条数
        return 1;
    }

    public void updateOrderGoodsApplyStatus(String orderNo, List<Long> orderGoodsIds, int status) {
        if (!CollectionUtils.isEmpty(orderGoodsIds)) {
            for (Long id : orderGoodsIds) {
                LambdaUpdateWrapper<GbOrderGoodsInfo> updateGoodsWrapper = Wrappers.lambdaUpdate();
                updateGoodsWrapper.eq(GbOrderGoodsInfo::getId, id);
                updateGoodsWrapper.eq(GbOrderGoodsInfo::getOrderNo, orderNo);
                updateGoodsWrapper.set(GbOrderGoodsInfo::getApplyRefund, status);
                goodsMapper.update(updateGoodsWrapper);
            }
        }
    }

    public int getOrderGoodsStatus(String orderNo) {
        LambdaQueryWrapper<GbOrderGoodsInfo> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.eq(GbOrderGoodsInfo::getOrderNo, orderNo);
        queryWrapper2.orderByAsc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(queryWrapper2);
        if (!CollectionUtils.isEmpty(goodsList)) {
            for (GbOrderGoodsInfo goods : goodsList) {
                int goodsNum = goods.getGoodsNum().intValue();
                int refundNum = goods.getRefundGoodsNum();
                if (goodsNum != refundNum) {
                    //只要该订单下最少有一个订单商品没有完全退，那么不改主订单状态
                    return 1;
                }
            }
        }
        return 0;
    }

    //用户订单列表查询
    public List<GbOrderInfo> getMemberOrderList(Long memberId, Integer status, String goodsName, int page, int pageSize) {
        List<GbOrderInfo> result;
        // 1. 查询符合条件的订单(分页)
        if (StringUtils.isEmpty(goodsName)) {
            LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(GbOrderInfo::getMemberId, memberId);
            if (status != null) {
                queryWrapper.eq(GbOrderInfo::getStatus, status);
            }
            queryWrapper.orderByDesc(GbOrderInfo::getId);
            queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
            result = mapper.selectList(queryWrapper);
        } else {
            // 先按商品名称模糊查询订单商品, 拿到命中的订单号
            LambdaQueryWrapper<GbOrderGoodsInfo> goodsWrapper = Wrappers.lambdaQuery();
            goodsWrapper.like(GbOrderGoodsInfo::getGoodsName, goodsName);
            goodsWrapper.orderByDesc(GbOrderGoodsInfo::getId);
            List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(goodsWrapper);
            if (CollectionUtils.isEmpty(goodsList)) {
                return new ArrayList<>();
            }
            List<String> orderNoList = goodsList.stream()
                    .map(GbOrderGoodsInfo::getOrderNo)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(orderNoList)) {
                return new ArrayList<>();
            }
            LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(GbOrderInfo::getMemberId, memberId);
            queryWrapper.in(GbOrderInfo::getOrderNo, orderNoList);
            if (status != null) {
                queryWrapper.eq(GbOrderInfo::getStatus, status);
            }
            queryWrapper.orderByDesc(GbOrderInfo::getId);
            queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
            result = mapper.selectList(queryWrapper);
        }
        if (CollectionUtils.isEmpty(result)) {
            return new ArrayList<>();
        }

        // 2. 批量查询本页订单的商品信息, 按 orderNo 关联
        List<String> orderNos = result.stream()
                .map(GbOrderInfo::getOrderNo)
                .collect(Collectors.toList());
        LambdaQueryWrapper<GbOrderGoodsInfo> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.in(GbOrderGoodsInfo::getOrderNo, orderNos);
        queryWrapper2.orderByDesc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(queryWrapper2);

        Map<String, List<GbOrderGoodsInfo>> goodsMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(goodsList)) {
            for (GbOrderGoodsInfo item : goodsList) {
                String tempNo = item.getOrderNo();
                if (goodsMap.containsKey(tempNo)) {
                    goodsMap.get(tempNo).add(item);
                } else {
                    List<GbOrderGoodsInfo> tempList = new ArrayList<>();
                    tempList.add(item);
                    goodsMap.put(tempNo, tempList);
                }
            }
        }

        for (GbOrderInfo item : result) {
            item.setGoodsInfoList(goodsMap.getOrDefault(item.getOrderNo(), new ArrayList<>()));
        }

        return result;
    }

    public List<GbOrderInfo> getMemberApplyRefundOrderList(Long memberId, Integer status, int page, int pageSize) {
        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getMemberId, memberId);
        queryWrapper.eq(GbOrderInfo::getStatus, OrderStatusEnum.APPLY_REFUND);
        queryWrapper.orderByDesc(GbOrderInfo::getId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbOrderInfo> orderList = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(orderList)) {
            return new ArrayList<>();
        }
        // 2. 批量查询本页订单的商品信息, 按 orderNo 关联
        List<String> orderNos = orderList.stream()
                .map(GbOrderInfo::getOrderNo)
                .collect(Collectors.toList());

        LambdaQueryWrapper<GbOrderGoodsInfo> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.in(GbOrderGoodsInfo::getOrderNo, orderNos);
        if (status != null) {
            queryWrapper2.eq(GbOrderGoodsInfo::getApplyRefund, status);
        }
        queryWrapper2.orderByDesc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(queryWrapper2);
        if (CollectionUtils.isEmpty(goodsList)) {
            return new ArrayList<>();
        }
        Map<Long, List<GbOrderGoodsInfo>> goodsMap = new HashMap<>();
        for (GbOrderGoodsInfo item : goodsList) {
            Long tempId = item.getId();
            if (goodsMap.containsKey(tempId)) {
                goodsMap.get(tempId).add(item);
            } else {
                List<GbOrderGoodsInfo> tempList = new ArrayList<>();
                tempList.add(item);
                goodsMap.put(tempId, tempList);
            }
        }
        List<GbOrderInfo> result = new ArrayList<>();
        for (GbOrderInfo item : orderList) {
            Long tempId = item.getId();
            if (goodsMap.containsKey(tempId)) {
                item.setGoodsInfoList(goodsMap.get(tempId));
                result.add(item);
            }
        }
        return result;
    }

    public List<GbOrderInfo> getLeaderOrderList(Long leaderId, Long groupId, String keyword,
                                                Integer status, int page, int pageSize) {
        // 参数防御: 避免 limit 偏移量出现负数, pageSize 限制上限
        page = Math.max(page, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 100);

        String trimKeyword = StringUtils.isEmpty(keyword) ? null : keyword.trim();
        // keyword 为纯数字时视为手机号, 否则视为商品名称
        boolean isPhone = trimKeyword != null && trimKeyword.matches("\\d+");

        List<GbOrderInfo> result;
        // 1. 查询符合条件的订单(分页)
        if (trimKeyword == null || isPhone) {
            // keyword 为空: 查全部; keyword 为手机号: 按手机号模糊匹配
            LambdaQueryWrapper<GbOrderInfo> queryWrapper = buildLeaderOrderQueryWrapper(leaderId, groupId, status, null);
            if (isPhone) {
                queryWrapper.like(GbOrderInfo::getMobile, trimKeyword);
            }
            queryWrapper.orderByDesc(GbOrderInfo::getId);
            queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
            result = mapper.selectList(queryWrapper);
        } else {
            // 先按商品名称模糊查询订单商品, 拿到命中的订单号
            LambdaQueryWrapper<GbOrderGoodsInfo> goodsWrapper = Wrappers.lambdaQuery();
            goodsWrapper.like(GbOrderGoodsInfo::getGoodsName, trimKeyword);
            goodsWrapper.orderByDesc(GbOrderGoodsInfo::getId);
            List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(goodsWrapper);
            List<String> orderNoList = goodsList.stream()
                    .map(GbOrderGoodsInfo::getOrderNo)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(orderNoList)) {
                return new ArrayList<>();
            }
            LambdaQueryWrapper<GbOrderInfo> queryWrapper = buildLeaderOrderQueryWrapper(leaderId, groupId, status, orderNoList);
            queryWrapper.orderByDesc(GbOrderInfo::getId);
            queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
            result = mapper.selectList(queryWrapper);
        }
        if (CollectionUtils.isEmpty(result)) {
            return new ArrayList<>();
        }

        // 2. 批量查询本页订单的商品信息, 按 orderNo 关联
        List<String> orderNos = result.stream()
                .map(GbOrderInfo::getOrderNo)
                .collect(Collectors.toList());
        LambdaQueryWrapper<GbOrderGoodsInfo> goodsWrapper = Wrappers.lambdaQuery();
        goodsWrapper.in(GbOrderGoodsInfo::getOrderNo, orderNos);
        goodsWrapper.orderByDesc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(goodsWrapper);

        Map<String, List<GbOrderGoodsInfo>> goodsMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(goodsList)) {
            for (GbOrderGoodsInfo item : goodsList) {
                goodsMap.computeIfAbsent(item.getOrderNo(), k -> new ArrayList<>()).add(item);
            }
        }

        for (GbOrderInfo item : result) {
            item.setGoodsInfoList(goodsMap.getOrDefault(item.getOrderNo(), new ArrayList<>()));
        }

        return result;
    }

    // 构造团长订单查询条件(公共部分)
    private LambdaQueryWrapper<GbOrderInfo> buildLeaderOrderQueryWrapper(Long leaderId, Long groupId,
                                                                         Integer status, List<String> orderNos) {
        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getLeaderId, leaderId);
        if (!CollectionUtils.isEmpty(orderNos)) {
            queryWrapper.in(GbOrderInfo::getOrderNo, orderNos);
        }
        if (groupId != null && groupId > 0) {
            queryWrapper.eq(GbOrderInfo::getGroupId, groupId);
        }
        if (status != null) {
            queryWrapper.eq(GbOrderInfo::getStatus, status);
        }
        return queryWrapper;
    }

    public List<GbOrderInfo> getLeaderApplyRefundOrderList(Long leaderId, Long groupId, String keyword,
                                                           Integer applyStatus, int page, int pageSize) {
        // 1. 查询售后订单(分页)
        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getLeaderId, leaderId);
        queryWrapper.eq(GbOrderInfo::getStatus, OrderStatusEnum.APPLY_REFUND.getCode());
        if (groupId != null && groupId > 0) {
            queryWrapper.eq(GbOrderInfo::getGroupId, groupId);
        }
        // keyword 为纯数字(手机号)时, 按手机号过滤订单
        if (!StringUtils.isEmpty(keyword) && isMobileKeyword(keyword)) {
            queryWrapper.like(GbOrderInfo::getMobile, keyword);
        }
        queryWrapper.orderByDesc(GbOrderInfo::getId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbOrderInfo> orderList = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(orderList)) {
            return new ArrayList<>();
        }

        // 2. 批量查询本页订单的商品信息, 按 orderNo 关联
        List<String> orderNos = orderList.stream()
                .map(GbOrderInfo::getOrderNo)
                .collect(Collectors.toList());
        LambdaQueryWrapper<GbOrderGoodsInfo> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.in(GbOrderGoodsInfo::getOrderNo, orderNos);
        if (applyStatus != null) {
            queryWrapper2.eq(GbOrderGoodsInfo::getApplyRefund, applyStatus);
        }
        // keyword 手机号(商品名称)时, 按商品名称过滤
        if (!StringUtils.isEmpty(keyword) && !isMobileKeyword(keyword)) {
            queryWrapper2.like(GbOrderGoodsInfo::getGoodsName, keyword);
        }
        queryWrapper2.orderByDesc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(queryWrapper2);
        if (CollectionUtils.isEmpty(goodsList)) {
            return new ArrayList<>();
        }

        Map<String, List<GbOrderGoodsInfo>> goodsMap = new HashMap<>();
        for (GbOrderGoodsInfo item : goodsList) {
            String tempNo = item.getOrderNo();
            if (goodsMap.containsKey(tempNo)) {
                goodsMap.get(tempNo).add(item);
            } else {
                List<GbOrderGoodsInfo> tempList = new ArrayList<>();
                tempList.add(item);
                goodsMap.put(tempNo, tempList);
            }
        }

        List<GbOrderInfo> result = new ArrayList<>();
        for (GbOrderInfo item : orderList) {
            String tempNo = item.getOrderNo();
            if (goodsMap.containsKey(tempNo)) {
                item.setGoodsInfoList(goodsMap.get(tempNo));
                result.add(item);
            }
        }
        return result;
    }

    // 判断 keyword 是否为手机号(纯数字)
    private Boolean isMobileKeyword(String keyword) {
        if (StringUtils.isEmpty(keyword)) {
            return false;
        }
        //return keyword.trim().matches("\\d+");
        return keyword.matches("^1[3-9]\\d{9}$");
    }

    public Integer getSumOfGroupActivityOrder(Long groupId) {
        return mapper.getSumOfGroupActivityOrder(groupId);
    }

    /**
     * 查询团购活动的真实跟团记录
     *
     * @param groupId 团购活动id
     * @param limit   返回条数，默认20，最大50
     * @return 跟团记录列表
     */
    public List<GroupOrderRecordResponse> getGroupOrderRecordList(Long groupId, Integer limit) {

        List<GroupOrderRecordResponse> result = new ArrayList<>();
        if (groupId == null || groupId <= 0) {
            return result;
        }
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        limit = Math.min(limit, 50);

        // 已支付/待收货/部分收货/已提货 视为有效订单
        LambdaQueryWrapper<GbOrderInfo> orderWrapper = Wrappers.lambdaQuery();
        orderWrapper.eq(GbOrderInfo::getGroupId, groupId);
        orderWrapper.in(GbOrderInfo::getStatus, Arrays.asList(1, 2, 3));
        orderWrapper.orderByDesc(GbOrderInfo::getId);
        orderWrapper.last("limit 0," + limit);
        List<GbOrderInfo> orderList = mapper.selectList(orderWrapper);
        if (CollectionUtils.isEmpty(orderList)) {
            return result;
        }

        List<String> orderNos = orderList.stream()
                .map(GbOrderInfo::getOrderNo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<String, GbOrderInfo> orderMap = orderList.stream()
                .collect(Collectors.toMap(GbOrderInfo::getOrderNo, v -> v, (v1, v2) -> v1));

        LambdaQueryWrapper<GbOrderGoodsInfo> goodsWrapper = Wrappers.lambdaQuery();
        goodsWrapper.in(GbOrderGoodsInfo::getOrderNo, orderNos);
        goodsWrapper.orderByAsc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(goodsWrapper);
        if (CollectionUtils.isEmpty(goodsList)) {
            return result;
        }

        for (GbOrderGoodsInfo goods : goodsList) {
            GbOrderInfo order = orderMap.get(goods.getOrderNo());
            if (order == null) {
                continue;
            }
            Integer addTime = order.getAddTime();
            result.add(new GroupOrderRecordResponse(
                    order.getMemberId(),
                    hideMobile(order.getMobile()),
                    order.getAvatar(),
                    order.getNickname(),
                    TimeUtils.getRelativeTime(addTime == null ? 0 : addTime),
                    buildGroupRecordGoodsDesc(goods),
                    goods.getGoodsNum()
            ));
        }
        return result;
    }

    /**
     * 手机号脱敏：只展示前2位和后4位，中间5位用*代替
     * 例如：13812345678 -> 13*****5678
     */
    private String hideMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return "";
        }
        mobile = mobile.trim();
        if (mobile.length() == 11) {
            return mobile.substring(0, 2) + "*****" + mobile.substring(7);
        }
        return mobile;
    }

    private String buildGroupRecordGoodsDesc(GbOrderGoodsInfo goods) {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(goods.getGoodsName())) {
            sb.append(goods.getGoodsName());
        }
        if (!StringUtils.isEmpty(goods.getSkuNames())) {
            if (sb.length() > 0) {
                sb.append("/");
            }
            sb.append(goods.getSkuNames());
        }
        return sb.toString();
    }

    // ======================= 团长端-我的团员相关接口 =======================

    /**
     * 团长端-我的团员列表
     */
    public List<LeaderMemberListResponse> getLeaderMemberList(Long leaderId, String keyword, Integer page, Integer pageSize) {
        List<LeaderMemberListResponse> result = new ArrayList<>();
        if (leaderId == null || leaderId <= 0) {
            return result;
        }
        int currentPage = page == null || page <= 0 ? 1 : page;
        int size = pageSize == null || pageSize <= 0 ? 10 : Math.min(pageSize, 100);
        int offset = (currentPage - 1) * size;

        List<Map<String, Object>> summaryList = mapper.getLeaderMemberSummaryList(leaderId, keyword, offset, size);
        if (CollectionUtils.isEmpty(summaryList)) {
            return result;
        }
        // 当前页成员id集合(批量查询查看次数/最近查看)
        List<Long> memberIds = new ArrayList<>();
        for (Map<String, Object> item : summaryList) {
            memberIds.add(toLong(item.get("memberId")));
        }
        Map<Long, Integer> viewCountMap = viewLogService.getViewCountMap(leaderId, memberIds);
        Map<Long, Map<String, Object>> lastViewMap = viewLogService.getLastViewMap(leaderId, memberIds);

        for (Map<String, Object> item : summaryList) {
            Long memberId = toLong(item.get("memberId"));
            String mobile = toStr(item.get("mobile"));
            String nickname = toStr(item.get("nickname"));
            String avatar = toStr(item.get("avatar"));
            Integer orderCount = toInt(item.get("orderCount"));
            Integer totalPayFee = toInt(item.get("totalPayFee"));
            Integer lastOrderTime = toInt(item.get("lastTime"));
            String lastGroupName = toStr(item.get("lastGroupName"));
            Integer viewCount = viewCountMap.get(memberId) == null ? 0 : viewCountMap.get(memberId);

            // 最近一次动态: 对比最近一次"查看"与最近一次"跟团下单", 取时间更近的
            int lastViewTime = 0;
            Map<String, Object> lastViewRow = lastViewMap.get(memberId);
            if (lastViewRow != null) {
                lastViewTime = toInt(lastViewRow.get("viewTime"));
            }
            String lastActionDesc = "";
            int latestTime = lastOrderTime == null ? 0 : lastOrderTime;
            if (lastViewTime > latestTime) {
                String viewGroupName = toStr(lastViewRow.get("groupName"));
                lastActionDesc = StringUtils.isEmpty(viewGroupName) ? "查看了团购页面" : "查看了" + viewGroupName + "团";
                latestTime = lastViewTime;
            } else if (latestTime > 0) {
                lastActionDesc = StringUtils.isEmpty(lastGroupName) ? "跟团下单" : "跟团下单 " + lastGroupName;
            }

            result.add(new LeaderMemberListResponse(
                    memberId,
                    hideMobile(mobile),
                    nickname,
                    avatar,
                    TimeUtils.getRelativeTime(latestTime),
                    lastActionDesc,
                    fenToYuan(totalPayFee),
                    orderCount == null ? 0 : orderCount,
                    viewCount
            ));
        }
        return result;
    }

    /**
     * 团长端-团员详情（含统计与动态）
     */
    public LeaderMemberDetailResponse getLeaderMemberDetail(Long leaderId, Long memberId) {
        if (leaderId == null || leaderId <= 0 || memberId == null || memberId <= 0) {
            return new LeaderMemberDetailResponse();
        }
        List<GbOrderInfo> orders = mapper.getLeaderMemberOrderList(leaderId, memberId);
        // 查看记录(埋点)最近50条
        List<GbGroupViewLog> views = viewLogService.getRecentViewList(leaderId, memberId);
        if (CollectionUtils.isEmpty(orders) && CollectionUtils.isEmpty(views)) {
            return new LeaderMemberDetailResponse(memberId, "", "", "", "0.00", "0.00", 0, 0, new ArrayList<>());
        }

        int orderCount = CollectionUtils.isEmpty(orders) ? 0 : orders.size();
        int totalPayFee = CollectionUtils.isEmpty(orders) ? 0
                : orders.stream().mapToInt(o -> o.getPayFee() == null ? 0 : o.getPayFee()).sum();
        int totalRefundFee = CollectionUtils.isEmpty(orders) ? 0
                : orders.stream().mapToInt(o -> o.getRefundFee() == null ? 0 : o.getRefundFee()).sum();
        // 查看次数(不受动态截断影响, 全量统计)
        Integer viewCount = viewLogService.getViewCount(leaderId, memberId);

        // 生成动态: 跟团下单 + 查看团购, 按时间从新到旧合并
        List<DynamicEntry> entries = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orders)) {
            for (GbOrderInfo order : orders) {
                Integer addTime = order.getAddTime();
                if (addTime == null || addTime <= 0) {
                    continue;
                }
                String groupName = StringUtils.isEmpty(order.getGroupName()) ? "" : order.getGroupName();
                entries.add(new DynamicEntry(addTime, "order", "跟团下单 " + groupName));
            }
        }
        if (!CollectionUtils.isEmpty(views)) {
            for (GbGroupViewLog view : views) {
                Integer viewTime = view.getViewTime();
                if (viewTime == null || viewTime <= 0) {
                    continue;
                }
                String groupName = StringUtils.isEmpty(view.getGroupName()) ? "" : view.getGroupName();
                String content = StringUtils.isEmpty(groupName) ? "查看了团购页面" : "查看了" + groupName + "团";
                entries.add(new DynamicEntry(viewTime, "view", content));
            }
        }
        if (entries.size() > DETAIL_DYNAMIC_LIMIT) {
            entries = new ArrayList<>(entries.subList(0, DETAIL_DYNAMIC_LIMIT));
        }
        List<MemberDynamicGroup> dynamicList = buildDynamicGroups(entries);

        // 用户信息: 优先最近一笔订单冗余, 其次最近一条查看记录冗余
        String mobile = "";
        String nickname = "";
        String avatar = "";
        if (!CollectionUtils.isEmpty(orders)) {
            GbOrderInfo latest = orders.get(0);
            mobile = latest.getMobile();
            nickname = latest.getNickname();
            avatar = latest.getAvatar();
        } else if (!CollectionUtils.isEmpty(views)) {
            GbGroupViewLog latestView = views.get(0);
            mobile = latestView.getMobile();
            nickname = latestView.getNickname();
            avatar = latestView.getAvatar();
        }
        return new LeaderMemberDetailResponse(
                memberId,
                hideMobile(mobile),
                nickname,
                avatar,
                fenToYuan(totalPayFee),
                fenToYuan(totalRefundFee),
                orderCount,
                viewCount,
                dynamicList
        );
    }

    // 详情动态最多展示条数
    private static final int DETAIL_DYNAMIC_LIMIT = 100;

    /**
     * 动态条目按时间倒序后按天分组
     */
    private List<MemberDynamicGroup> buildDynamicGroups(List<DynamicEntry> entries) {
        Map<String, MemberDynamicGroup> groupMap = new LinkedHashMap<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        entries.sort((a, b) -> Integer.compare(b.timestamp, a.timestamp));
        for (DynamicEntry entry : entries) {
            if (entry.timestamp <= 0) {
                continue;
            }
            String dateKey = formatDynamicDateKey(entry.timestamp);
            String dateLabel = formatDynamicDateLabel(entry.timestamp);
            MemberDynamicGroup group = groupMap.computeIfAbsent(dateKey, k -> new MemberDynamicGroup(dateLabel, new ArrayList<>()));
            String timeLabel = timeFormat.format(new Date(entry.timestamp * 1000L));
            group.getItems().add(new MemberDynamicItem(timeLabel, entry.action, entry.content));
        }
        return new ArrayList<>(groupMap.values());
    }

    /**
     * 团员动态条目
     */
    private static class DynamicEntry {
        private final int timestamp;
        private final String action;
        private final String content;

        DynamicEntry(int timestamp, String action, String content) {
            this.timestamp = timestamp;
            this.action = action;
            this.content = content;
        }
    }

    private String fenToYuan(Integer fen) {
        if (fen == null) {
            return "0.00";
        }
        return BigDecimal.valueOf(fen).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).toString();
    }

    private Long toLong(Object obj) {
        if (obj == null) {
            return 0L;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.valueOf(obj.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private Integer toInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.valueOf(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String toStr(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    private String formatDynamicDateKey(int timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp * 1000L);
        return String.format("%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    private String formatDynamicDateLabel(int timestamp) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(timestamp * 1000L);

        if (isSameDay(now, target)) {
            return "今天";
        }
        now.add(Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(now, target)) {
            return "昨天";
        }
        if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR)) {
            return String.format("%02d-%02d", target.get(Calendar.MONTH) + 1, target.get(Calendar.DAY_OF_MONTH));
        }
        return String.format("%04d-%02d-%02d", target.get(Calendar.YEAR), target.get(Calendar.MONTH) + 1, target.get(Calendar.DAY_OF_MONTH));
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}
