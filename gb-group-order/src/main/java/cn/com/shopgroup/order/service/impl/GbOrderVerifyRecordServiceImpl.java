package cn.com.shopgroup.order.service.impl;

import cn.com.shopgroup.order.mapper.GbOrderVerifyRecordMapper;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.model.GbOrderVerifyRecord;
import cn.com.shopgroup.order.service.GbOrderVerifyRecordService;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 订单核销记录表: 核销成功后落库, 记录订单主要信息/核销商品明细/核销人/核销类型(0团长后台核销, 2用户扫码核销)
@Slf4j
@Service
public class GbOrderVerifyRecordServiceImpl implements GbOrderVerifyRecordService {

    @Resource
    private GbOrderVerifyRecordMapper mapper;

    // 根据订单号查询核销记录(支持一单多次部分核销, 按时间正序)
    @Override
    public List<GbOrderVerifyRecord> getVerifyRecordListByOrderNo(String orderNo) {

        LambdaQueryWrapper<GbOrderVerifyRecord> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderVerifyRecord::getOrderNo, orderNo);
        queryWrapper.orderByAsc(GbOrderVerifyRecord::getId);
        List<GbOrderVerifyRecord> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    // 新增核销记录: 记录失败仅记日志不抛出, 不影响核销主流程
    @Override
    public int addVerifyRecord(GbOrderVerifyRecord record) {

        if (record == null || record.getOrderNo() == null || record.getOrderNo().length() == 0) {
            return 0;
        }
        try {
            if (record.getAddTime() == null) {
                record.setAddTime((int) (System.currentTimeMillis() / 1000));
            }
            return mapper.insert(record);
        } catch (Exception e) {
            log.error("核销记录落库失败, orderNo:{}", record.getOrderNo(), e);
            return 0;
        }
    }

    // 构建核销记录: 订单主要信息 + 核销商品明细 + 核销人 + 核销类型
    @Override
    public GbOrderVerifyRecord buildVerifyRecord(GbOrderInfo orderInfo, List<GbOrderGoodsInfo> goodsList,
                                                 Map<Long, Integer> verifyNumMap, int verifyType,
                                                 Long staffId, String staffName, Long verifyPointId, String verifyPointName) {
        GbOrderVerifyRecord record = new GbOrderVerifyRecord();
        if (orderInfo == null) {
            return record;
        }
        // ---------- 订单主要信息 ----------
        record.setOrderNo(orderInfo.getOrderNo());
        record.setMemberId(orderInfo.getMemberId() == null ? 0L : orderInfo.getMemberId());
        record.setNickname(orderInfo.getNickname());
        record.setMobile(orderInfo.getMobile());
        record.setLeaderId(orderInfo.getLeaderId() == null ? 0L : orderInfo.getLeaderId());
        record.setGroupId(orderInfo.getGroupId() == null ? 0L : orderInfo.getGroupId());
        record.setGroupName(orderInfo.getGroupName());
        record.setPointId(orderInfo.getPointId() == null ? 0L : orderInfo.getPointId());
        record.setPointName(orderInfo.getPointName());
        record.setPayFee(orderInfo.getPayFee() == null ? 0 : orderInfo.getPayFee());
        record.setReceiptCode(orderInfo.getReceiptCode());
        // ---------- 核销类型/核销人/核销自提点 ----------
        record.setVerifyType(verifyType);
        record.setStaffId(staffId == null ? 0L : staffId);
        record.setStaffName(staffName == null ? "" : staffName);
        record.setVerifyPointId(verifyPointId == null ? 0L : verifyPointId);
        record.setVerifyPointName(verifyPointName == null ? "" : verifyPointName);
        // ---------- 核销商品明细 ----------
        record.setVerifyGoodsMsg(buildVerifyGoodsMsg(goodsList, verifyNumMap));
        return record;
    }

    // 核销商品明细(JSON数组, 仅保留本次核销数量>0的商品行):
    // 每行 goodsId/goodsName/skuNames/goodsPrice(元)/goodsUnit/goodsNum(购买数)/verifyNum(本次核销数)/receiptNum(累计已核销数)
    private String buildVerifyGoodsMsg(List<GbOrderGoodsInfo> goodsList, Map<Long, Integer> verifyNumMap) {

        if (CollectionUtils.isEmpty(goodsList)) {
            return "[]";
        }
        List<Map<String, Object>> detailList = new ArrayList<>();
        for (GbOrderGoodsInfo goods : goodsList) {
            int goodsNum = goods.getGoodsNum() == null ? 0 : goods.getGoodsNum();
            int receiptNum = goods.getReceiptNum() == null ? 0 : goods.getReceiptNum();
            int refundNum = goods.getRefundNum() == null ? 0 : goods.getRefundNum();
            boolean hasVerifyNum = verifyNumMap != null && verifyNumMap.containsKey(goods.getId());
            // 本次核销数量: 部分核销取入参; 整单/用户核销(未传)按剩余可核销数(购买数-已核销-已退)推算
            int verifyNum = hasVerifyNum
                    ? (verifyNumMap.get(goods.getId()) == null ? 0 : verifyNumMap.get(goods.getId()))
                    : Math.max(goodsNum - receiptNum - refundNum, 0);
            if (verifyNum <= 0) {
                // 本次未核销的商品行不记录
                continue;
            }
            // 累计已核销数: 部分核销入参时商品行receipt_num已含本次, 直接取; 否则(核销前值)+本次核销数
            int totalReceiptNum = hasVerifyNum ? receiptNum : receiptNum + verifyNum;
            Map<String, Object> detail = new HashMap<>();
            detail.put("goodsId", goods.getGoodsId());
            detail.put("goodsName", goods.getGoodsName());
            detail.put("skuNames", goods.getSkuNames());
            detail.put("goodsPrice", goods.getGoodsPrice());
            detail.put("goodsUnit", goods.getGoodsUnit());
            detail.put("goodsNum", goodsNum);
            detail.put("verifyNum", verifyNum);
            detail.put("receiptNum", totalReceiptNum);
            detailList.add(detail);
        }
        return JSON.toJSONString(detailList);
    }
}
