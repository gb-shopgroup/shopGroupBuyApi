package cn.com.shopgroup.order.service;

import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.model.GbOrderVerifyRecord;

import java.util.List;
import java.util.Map;

// 订单核销记录表
public interface GbOrderVerifyRecordService {

    // 核销类型: 0=团长后台核销(整单/部分核销)
    int VERIFY_TYPE_LEADER = 0;
    // 核销类型: 2=用户扫码核销(用户主动确认收货)
    int VERIFY_TYPE_MEMBER = 2;

    // 根据订单号查询核销记录(支持一单多次部分核销, 产生多条记录)
    List<GbOrderVerifyRecord> getVerifyRecordListByOrderNo(String orderNo);

    // 新增核销记录(落库失败仅记日志, 不影响核销主流程)
    int addVerifyRecord(GbOrderVerifyRecord record);

    // 构建核销记录: 订单主要信息 + 核销商品明细 + 核销人 + 核销类型
    // verifyNumMap: key=订单商品id, value=本次核销数量(部分核销入参, 商品行receipt_num已含本次核销时必须传入);
    //               传null时按"剩余可核销数(购买数-已核销-已退)"整单口径推算本次核销数量
    GbOrderVerifyRecord buildVerifyRecord(GbOrderInfo orderInfo, List<GbOrderGoodsInfo> goodsList,
                                          Map<Long, Integer> verifyNumMap, int verifyType,
                                          Long staffId, String staffName, Long verifyPointId, String verifyPointName);
}
