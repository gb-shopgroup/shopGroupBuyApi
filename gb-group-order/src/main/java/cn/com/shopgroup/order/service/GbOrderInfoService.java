package cn.com.shopgroup.order.service;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.order.constants.OrderStatusEnum;
import cn.com.shopgroup.order.http.response.GroupOrderRecordResponse;
import cn.com.shopgroup.order.http.response.LeaderMemberDetailResponse;
import cn.com.shopgroup.order.http.response.LeaderMemberListResponse;
import cn.com.shopgroup.order.http.response.MemberDynamicGroup;
import cn.com.shopgroup.order.http.response.MemberDynamicItem;
import cn.com.shopgroup.order.http.response.RefundOrderGoodsResponse;
import cn.com.shopgroup.order.http.response.RefundOrderInfoResponse;
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
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
        if (memberId != null && memberId.intValue() > 0) {
            queryWrapper.eq(GbOrderInfo::getMemberId, memberId);
        }
        queryWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        GbOrderInfo data = mapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(data)) {
            return null;
        }
        LambdaQueryWrapper<GbOrderGoodsInfo> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.eq(GbOrderGoodsInfo::getOrderNo, orderNo);
        queryWrapper2.orderByAsc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(queryWrapper2);

        if (!CollectionUtils.isEmpty(goodsList)) {
            data.setGoodsInfoList(goodsList);
        }


        return data;
    }


    public boolean miniBusinessOrder(String orderNo, Long busId, String merchantNo) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderInfo::getBusId, busId);
        updateWrapper.set(GbOrderInfo::getMerchantNo, merchantNo);
        updateWrapper.set(GbOrderInfo::getUpdateTime, TimeUtils.getTimeStamp());
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
        updateWrapper.set(GbOrderInfo::getUpdateTime, TimeUtils.getTimeStamp());
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
        updateWrapper.set(GbOrderInfo::getUpdateTime, TimeUtils.getTimeStamp());
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    // 用户申请退款: 仅把订单置为售后(5)待团长审核并记录申请时间; 主表退费金额refund_fee不在申请时维护(占坑),
    // 待团长同意且退款成功后, 才由 addMiniOrderRefundFee 累加到订单主表; 拒绝时金额无需回退即回到申请前
    public Boolean miniRefundOrder(Long memberId, String orderNo) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.eq(GbOrderInfo::getMemberId, memberId);
        updateWrapper.set(GbOrderInfo::getStatus, OrderStatusEnum.APPLY_REFUND.getCode());
        updateWrapper.set(GbOrderInfo::getRefundTime, TimeUtils.getTimeStamp());
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }

    // 团长审核同意且退款成功后, 把本次申请退款金额(单位:分)累计维护到订单表refund_fee
    public Boolean addMiniOrderRefundFee(String orderNo, Integer addRefundFee) {
        if (orderNo == null || addRefundFee == null || addRefundFee <= 0) {
            return false;
        }
        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderInfo::getUpdateTime, TimeUtils.getTimeStamp());
        updateWrapper.setSql("refund_fee = IFNULL(refund_fee, 0) + " + addRefundFee);
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

    /**
     * 退款订单数量统计(团长端 /leader/refund/count):
     * 只要订单中存在商品发生过退款(审核同意)即计入, 不要求整单全部退完:
     * 1) 整单已退款: status=4(含历史老数据商品行未标售后状态的全退单)
     * 2) 仅部分商品退款成功: 订单主状态已恢复流转(1/2/3), 但商品行售后状态 apply_refund=2 保留
     * 注: 不限制 verify_time=0, 已核销后退货退款成功的订单同样计入; 售后待审核(apply_refund=1)/被拒(apply_refund=3)未产生真实退款, 不计入
     */
    public Long getMiniLeaderRefundOrderCount(Long leaderId, Long groupId, Long pointId) {

        QueryWrapper<GbOrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("leader_id", leaderId);
        if (groupId > 0) queryWrapper.eq("group_id", groupId);
        if (pointId > 0) queryWrapper.eq("point_id", pointId);
        // status=4(整单全退) 或 存在商品行已同意退款(部分退成功)
        queryWrapper.and(w -> w.eq("status", OrderStatusEnum.REFUNDED.getCode())
                .or().inSql("order_no", "select order_no from gb_order_goods_info where apply_refund = 2"));
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
        updateWrapper.set(GbOrderInfo::getUpdateTime, TimeUtils.getTimeStamp());
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
        updateWrapper.set(GbOrderInfo::getUpdateTime, TimeUtils.getTimeStamp());


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

    /**
     * 售后审核结束(拒绝退款 / 仅部分商品退款成功)后, 把仍停留在售后(5)的订单恢复为正常流转状态:
     * 前提: 订单状态仍为售后(5) 且 该订单已无待审核售后(apply_refund=1)的商品行, 避免打断仍在途的其它售后申请
     * 规则: 按用户确认收货时间(receipt_time)与商品行核销/退款完整度推算申请前状态
     * 1) receipt_time=0(未确认过收货): 恢复待收货(1), 用户可继续核销/确认收货; 已分账核销满7天的由定时任务自动完成
     * 2) receipt_time>0(确认过收货): 每行 核销量(receipt_num)+已退未收货量(refund_num)+已退已收货量(refund_goods_num) >= 购买量
     * => 全部商品已处理完, 恢复已提货(3); 否则恢复部分收货(2), 剩余部分继续核销并由定时任务自动完成
     */
    public void restoreOrderStatusAfterRefundReview(String orderNo) {
        if (orderNo == null) {
            return;
        }
        GbOrderInfo orderInfo = getOrderInfoByOrderNo(orderNo);
        if (orderInfo == null || orderInfo.getStatus() == null
                || orderInfo.getStatus().intValue() != OrderStatusEnum.APPLY_REFUND.getCode()) {
            return;
        }
        List<GbOrderGoodsInfo> goodsList = getOrderGoodsList(orderNo);
        if (CollectionUtils.isEmpty(goodsList)) {
            return;
        }
        // 该订单还有商品行售后待审核: 订单继续停留在售后, 待最后一笔审核结束再恢复
        for (GbOrderGoodsInfo goods : goodsList) {
            if (goods.getApplyRefund() != null && goods.getApplyRefund().intValue() == 1) {
                return;
            }
        }
        int targetStatus;
        int receiptTime = orderInfo.getReceiptTime() == null ? 0 : orderInfo.getReceiptTime();
        if (receiptTime > 0) {
            boolean allFinished = true;
            for (GbOrderGoodsInfo goods : goodsList) {
                int goodsNum = goods.getGoodsNum() == null ? 0 : goods.getGoodsNum();
                int receiptNum = goods.getReceiptNum() == null ? 0 : goods.getReceiptNum();
                int refundNum = goods.getRefundNum() == null ? 0 : goods.getRefundNum();
                int refundGoodsNum = goods.getRefundGoodsNum() == null ? 0 : goods.getRefundGoodsNum();
                if (receiptNum + refundNum + refundGoodsNum < goodsNum) {
                    allFinished = false;
                    break;
                }
            }
            targetStatus = allFinished ? OrderStatusEnum.RECEIVED.getCode() : OrderStatusEnum.PART_RECEIVED.getCode();
        } else {
            targetStatus = OrderStatusEnum.PREPAID.getCode();
        }
        // CAS 更新: 仅当订单仍处售后(5)时恢复, 避免覆盖已全退置退款(4)或并发审核已恢复过的订单
        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.eq(GbOrderInfo::getStatus, OrderStatusEnum.APPLY_REFUND.getCode());
        updateWrapper.set(GbOrderInfo::getStatus, targetStatus);
        updateWrapper.set(GbOrderInfo::getUpdateTime, TimeUtils.getTimeStamp());
        mapper.update(updateWrapper);
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


    // (团长端首页)商品统计汇总: 商品种类总数 + 待核销总件数
    public Map<String, Object> getSummaryGoodsTotal(Long leaderId, Long pointId, String keyword) {

        return mapper.getSummaryGoodsTotal(leaderId, pointId, keyword);
    }


    // (团长端首页)商品维度统计分页列表
    public List<Map<String, Object>> getSummaryGoodsPageList(Long leaderId, Long pointId, String keyword, int offset, int limit) {

        return mapper.getSummaryPointGoodsPageList(leaderId, pointId, keyword, offset, limit);
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
        updateWrapper.set(GbOrderInfo::getUpdateTime, TimeUtils.getTimeStamp());
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }

    // 根据订单号修改确认收货操作标记(click_confirm_flag:0=未操作,1=已操作)
    public Boolean updateClickConfirmFlag(String orderNo, Integer flag) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderInfo::getClickConfirmFlag, flag);
        updateWrapper.set(GbOrderInfo::getUpdateTime, TimeUtils.getTimeStamp());
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

    public int updateOrderGoodsRefundByOrderNo(List<GbOrderGoodsInfo> goodsList, int isReturnGoods) {
        if (CollectionUtils.isEmpty(goodsList)) {
            return 0;
        }
        for (GbOrderGoodsInfo item : goodsList) {
            LambdaUpdateWrapper<GbOrderGoodsInfo> updateGoodsWrapper = Wrappers.lambdaUpdate();
            updateGoodsWrapper.eq(GbOrderGoodsInfo::getId, item.getId());
            updateGoodsWrapper.set(GbOrderGoodsInfo::getApplyRefund, 1);
            if (isReturnGoods == 1) {
                updateGoodsWrapper.setSql("refund_num = refund_num + {0}", Math.abs(item.getRefundNum()));
            }
            if (isReturnGoods == 2) {
                updateGoodsWrapper.setSql("refund_goods_num = refund_goods_num + {0}", Math.abs(item.getRefundGoodsNum()));
            }
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

    /**
     * 累计订单商品的已退款数(refund_num): 仅在审核同意且退款成功后调用
     * 与申请数量(refund_goods_num)区分: refund_goods_num 提交申请即累加(含待审核/被拒),
     * 这里累加的是真正退款成功的数量
     *
     * @param goodsRefundNumMap key=订单商品id, value=本次退款成功数量
     */
    /**
     * 审核拒绝时回退商品行本次申请累计的退款/退货退款数量
     * 申请时商品行数量占坑(主表refund_fee金额改为团长同意后才累加, 不在申请时占坑),
     * 拒绝则按本次申请量回退, 否则占坑导致无法再次申请/数量虚高
     *
     * @param refundNumMap  key=订单商品id, value=本次申请数量(审核拒绝时从请求回传)
     * @param isReturnGoods 1=退款(退待收货部分, 回退refund_num) 2=退货退款(退已收货部分, 回退refund_goods_num)
     */
    public int deductOrderGoodsRefundByOrderNo(Map<Long, Integer> refundNumMap, int isReturnGoods) {
        if (refundNumMap == null || refundNumMap.isEmpty() || (isReturnGoods != 1 && isReturnGoods != 2)) {
            return 0;
        }
        for (Map.Entry<Long, Integer> entry : refundNumMap.entrySet()) {
            LambdaUpdateWrapper<GbOrderGoodsInfo> updateGoodsWrapper = Wrappers.lambdaUpdate();
            updateGoodsWrapper.eq(GbOrderGoodsInfo::getId, entry.getKey());
            int refundNum = entry.getValue() == null ? 0 : Math.abs(entry.getValue());
            if (refundNum <= 0) {
                continue;
            }
            if (isReturnGoods == 1) {
                // 扣减不足时置0, 避免出现负数
                updateGoodsWrapper.setSql("refund_num = IF(refund_num >= " + refundNum + ", refund_num - " + refundNum + ", 0)");
            } else {
                updateGoodsWrapper.setSql("refund_goods_num = IF(refund_goods_num >= " + refundNum + ", refund_goods_num - " + refundNum + ", 0)");
            }
            goodsMapper.update(updateGoodsWrapper);
        }
        return 1;
    }

    /**
     * 审核同意某批退款后判断订单商品是否全部退款完成:
     * 每个商品行必须已同意(apply_refund=2)且 退款数量(refund_num) + 退货退款数量(refund_goods_num) >= 购买数量
     * 0=全部退完, 1=还有未退完/未同意的商品
     */
    public int getOrderGoodsStatus(String orderNo) {
        LambdaQueryWrapper<GbOrderGoodsInfo> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.eq(GbOrderGoodsInfo::getOrderNo, orderNo);
        queryWrapper2.orderByAsc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(queryWrapper2);
        if (!CollectionUtils.isEmpty(goodsList)) {
            for (GbOrderGoodsInfo goods : goodsList) {
                int goodsNum = goods.getGoodsNum() == null ? 0 : goods.getGoodsNum();
                // 退款数量(退待收货部分, 申请累计)
                int refundNum = goods.getRefundNum() == null ? 0 : goods.getRefundNum();
                // 退货退款数量(退已收货部分, 申请累计)
                int refundGoodsNum = goods.getRefundGoodsNum() == null ? 0 : goods.getRefundGoodsNum();
                // 商品行未同意售后 或 申请数量未覆盖购买数量 -> 还有未退完的商品, 不改主订单状态
                if (goods.getApplyRefund() == null || goods.getApplyRefund() != 2 || refundNum + refundGoodsNum < goodsNum) {
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

    // 用户售后订单列表: 支持按商品名称模糊过滤; 同一订单不同商品可能处于不同售后(审核)状态
    // (1 待审核 2 同意 3 不同意), 因此按(订单,审核状态)拆分返回, 每种状态一行(该行goods为该状态商品子集)
    public List<GbOrderInfo> getMemberApplyRefundOrderList(Long memberId, Integer status, String goodsName, int page, int pageSize) {
        // 参数防御: 避免 limit 偏移量出现负数, pageSize 限制上限
        page = Math.max(page, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 100);
        String trimGoodsName = StringUtils.isEmpty(goodsName) ? null : goodsName.trim();
        // 售后(审核)状态: 1 待审核 2 同意 3 不同意; 不传=查询该用户所有存在售后记录(1/2/3)的订单
        List<Integer> statusScope = (status != null && status >= 1 && status <= 3)
                ? Arrays.asList(status)
                : Arrays.asList(1, 2, 3);

        // 1. 先按商品名称/售后(审核)状态过滤订单商品行, 拿到命中的订单号集合(仅限当前用户的订单)
        LambdaQueryWrapper<GbOrderGoodsInfo> hitGoodsWrapper = Wrappers.lambdaQuery();
        hitGoodsWrapper.in(GbOrderGoodsInfo::getApplyRefund, statusScope);
        if (trimGoodsName != null) {
            hitGoodsWrapper.like(GbOrderGoodsInfo::getGoodsName, trimGoodsName);
        }
        hitGoodsWrapper.inSql(GbOrderGoodsInfo::getOrderNo,
                "select order_no from gb_order_info where member_id = " + memberId);
        List<GbOrderGoodsInfo> hitGoodsList = goodsMapper.selectList(hitGoodsWrapper);
        if (CollectionUtils.isEmpty(hitGoodsList)) {
            return new ArrayList<>();
        }
        List<String> hitOrderNos = hitGoodsList.stream()
                .map(GbOrderGoodsInfo::getOrderNo)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 2. 用命中的订单号分页查询订单(按订单id倒序)
        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getMemberId, memberId);
        queryWrapper.in(GbOrderInfo::getOrderNo, hitOrderNos);
        queryWrapper.orderByDesc(GbOrderInfo::getId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbOrderInfo> orderList = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(orderList)) {
            return new ArrayList<>();
        }

        // 3. 批量查询本页订单对应的售后商品行(过滤条件与步骤1保持一致), 按 orderNo 关联
        List<String> orderNos = orderList.stream()
                .map(GbOrderInfo::getOrderNo)
                .collect(Collectors.toList());
        LambdaQueryWrapper<GbOrderGoodsInfo> goodsWrapper = Wrappers.lambdaQuery();
        goodsWrapper.in(GbOrderGoodsInfo::getOrderNo, orderNos);
        goodsWrapper.in(GbOrderGoodsInfo::getApplyRefund, statusScope);
        if (trimGoodsName != null) {
            goodsWrapper.like(GbOrderGoodsInfo::getGoodsName, trimGoodsName);
        }
        goodsWrapper.orderByDesc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(goodsWrapper);
        if (CollectionUtils.isEmpty(goodsList)) {
            return new ArrayList<>();
        }
        Map<String, List<GbOrderGoodsInfo>> goodsMap = new HashMap<>();
        for (GbOrderGoodsInfo item : goodsList) {
            String orderNo = item.getOrderNo();
            if (goodsMap.containsKey(orderNo)) {
                goodsMap.get(orderNo).add(item);
            } else {
                List<GbOrderGoodsInfo> tempList = new ArrayList<>();
                tempList.add(item);
                goodsMap.put(orderNo, tempList);
            }
        }

        // 4. 组装: 同一订单的商品可能处于不同售后(审核)状态, 按状态拆分成多行返回
        //    例: 同一订单3个商品状态分别为 1待审核/2同意/3不同意 时, 该订单拆成3条数据展示
        List<GbOrderInfo> result = new ArrayList<>();
        for (GbOrderInfo item : orderList) {
            List<GbOrderGoodsInfo> orderGoodsList = goodsMap.get(item.getOrderNo());
            if (CollectionUtils.isEmpty(orderGoodsList)) {
                continue;
            }
            // 按审核状态分组(状态顺序固定 1->2->3, 保证同一订单多行展示顺序稳定)
            Map<Integer, List<GbOrderGoodsInfo>> statusGroupMap = new LinkedHashMap<>();
            for (GbOrderGoodsInfo goods : orderGoodsList) {
                Integer goodsStatus = goods.getApplyRefund() == null ? 0 : goods.getApplyRefund();
                List<GbOrderGoodsInfo> groupList = statusGroupMap.get(goodsStatus);
                if (groupList == null) {
                    groupList = new ArrayList<>();
                    statusGroupMap.put(goodsStatus, groupList);
                }
                groupList.add(goods);
            }
            for (Integer groupStatus : statusScope) {
                List<GbOrderGoodsInfo> groupGoods = statusGroupMap.get(groupStatus);
                if (CollectionUtils.isEmpty(groupGoods)) {
                    continue;
                }
                GbOrderInfo orderView = new GbOrderInfo();
                BeanUtils.copyProperties(item, orderView);
                orderView.setGoodsInfoList(groupGoods);
                result.add(orderView);
            }
        }
        return result;
    }

    public List<GbOrderInfo> getLeaderOrderList(Long leaderId, Long groupId, Long pointId, String keyword,
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
            if (pointId != null && pointId > 0) {
                queryWrapper.eq(GbOrderInfo::getPointId, pointId);
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
            if (pointId != null && pointId > 0) {
                queryWrapper.eq(GbOrderInfo::getPointId, pointId);
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

    public List<GbOrderInfo> getAllByLeaderIdAndPointId(Long leaderId, Long pointId) {
        // 团长首页 head 订单统计: 查询该团长的有效订单(排除已取消),
        // pointId 有值时按提货点过滤
        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getLeaderId, leaderId);
        queryWrapper.ne(GbOrderInfo::getStatus, OrderStatusEnum.CANCELED.getCode());
        if (pointId != null && pointId > 0) {
            queryWrapper.eq(GbOrderInfo::getPointId, pointId);
        }
        queryWrapper.orderByDesc(GbOrderInfo::getId);
        List<GbOrderInfo> orderList = mapper.selectList(queryWrapper);
        return orderList == null ? new ArrayList<>() : orderList;
    }

    /**
     * 用户端-申请退款/退货前查询订单与可申请商品(快照)
     * 退款类型标识 refundFlag: 1=退款(仅退款, 商品未收货--仅退一次，数量待收货总数) 2=退货退款(商品已收货后退回)
     * - 退款(1): 只返回商品中"待收货"(购买数量-收货数量>0)的商品行
     * - 退货退款(2): 只返回商品中"已收货"(收货数量>0)的商品行
     * 订单不存在/不属于该用户/没有符合退款类型的商品时返回 null
     */
    public RefundOrderInfoResponse getApplyRefundOrderInfo(Long memberId, String orderNo, Integer refundFlag) {
        if (memberId == null || memberId <= 0 || StringUtils.isEmpty(orderNo)) {
            return null;
        }
        // 1. 校验订单归属(用户id+订单号, 与申请退款接口同口径)
        GbOrderInfo orderInfo = getMiniOrderInfo(memberId, orderNo);
        if (ObjectUtils.isEmpty(orderInfo)) {
            return null;
        }
        // 2. 查询该订单的商品明细
        List<GbOrderGoodsInfo> goodsList = getOrderGoodsList(orderNo);
        if (CollectionUtils.isEmpty(goodsList)) {
            return null;
        }
        // 3. 按退款类型过滤商品并计算每件商品的可退数量
        //    商品行口径: goods_num=购买数量, receipt_num=收货数量(已核销/确认收货),
        //    refund_num=退款数量(退待收货部分, 申请累计), refund_goods_num=退货退款数量(退已收货部分, 申请累计)
        //    退款(1): 可退"待收货(尚未收货)"部分 = 购买数量 - 收货数量 - 已申请退款数量
        //    退货退款(2): 可退"已收货"部分 = 收货数量 - 已申请退货退款数量
        int isReturnGoods = Optional.ofNullable(refundFlag).orElse(0).intValue();
        List<RefundOrderGoodsResponse> refundGoods = new ArrayList<>();
        for (GbOrderGoodsInfo goods : goodsList) {
            // 总购买数
            int goodsNum = goods.getGoodsNum() == null ? 0 : goods.getGoodsNum();
            //已收货数
            int receiptNum = goods.getReceiptNum() == null ? 0 : goods.getReceiptNum();
            //退货退款数（退已收货数）
            int refundedGoodNum = goods.getRefundGoodsNum() == null ? 0 : goods.getRefundGoodsNum();
            //退款数（退支付待收货）
            int refundedNum = goods.getRefundNum() == null ? 0 : goods.getRefundNum();
            int canRefundNum = 0;
            if (isReturnGoods == 2) {
                // 退货退款: 只查询"已经收货"的商品(收货数量>0), 已收货但未退部分可退
                if (receiptNum - refundedGoodNum > 0) {
                    canRefundNum = receiptNum - refundedGoodNum;
                }
            }
            if (isReturnGoods == 1) {
                // 退款: 只查询"待收货(尚未收货)"的商品(收货数量<购买数量), 待收货未退部分可退
                int m = goodsNum - receiptNum - refundedNum;
                if (m > 0) {
                    canRefundNum = m;
                }
            }
            // 该部分已退完(可退数量<=0)的商品不参与本次申请
            if (canRefundNum <= 0) {
                continue;
            }
            refundGoods.add(new RefundOrderGoodsResponse(goods, canRefundNum));
        }
        if (refundGoods.isEmpty()) {
            return null;
        }
        // 4. 组装响应: 订单信息 + 可退款商品列表(含各商品可退数量)
        RefundOrderInfoResponse data = new RefundOrderInfoResponse(orderInfo);
        data.setRefundGoods(refundGoods);
        return data;
    }

    public GbOrderInfo getApplyRefunOrderInfo(String orderNo) {


        LambdaQueryWrapper<GbOrderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderInfo::getStatus, 5);
        queryWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        GbOrderInfo data = mapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(data)) {
            return null;
        }
        LambdaQueryWrapper<GbOrderGoodsInfo> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.eq(GbOrderGoodsInfo::getOrderNo, orderNo);
        queryWrapper2.orderByAsc(GbOrderGoodsInfo::getId);
        List<GbOrderGoodsInfo> goodsList = goodsMapper.selectList(queryWrapper2);

        if (!CollectionUtils.isEmpty(goodsList)) {
            data.setGoodsInfoList(goodsList);
        }


        return data;
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
