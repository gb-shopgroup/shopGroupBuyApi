package cn.com.shopgroup.order.service;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.order.constants.OrderStatusEnum;
import cn.com.shopgroup.order.mapper.GbOrderGoodsInfoMapper;
import cn.com.shopgroup.order.mapper.GbOrderInfoMapper;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GbOrderInfoService {

    @Resource
    private GbOrderInfoMapper mapper;

    @Resource
    private GbOrderGoodsInfoMapper goodsMapper;

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
}
