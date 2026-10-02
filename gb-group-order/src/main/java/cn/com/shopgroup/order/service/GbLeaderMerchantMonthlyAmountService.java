package cn.com.shopgroup.order.service;

import cn.com.shopgroup.order.model.GbLeaderMerchantMonthlyAmount;

import java.math.BigDecimal;
import java.util.List;

public interface GbLeaderMerchantMonthlyAmountService {

    // 查询单个商户某月的收入记录(merchant_no + year_month 唯一)
    GbLeaderMerchantMonthlyAmount getByMerchantAndYearMonth(Long leaderId, String merchantNo, String yearMonth);

    // 查询团长某月下所有收款账户的收入记录
    List<GbLeaderMerchantMonthlyAmount> listByLeaderAndYearMonth(Long leaderId, String yearMonth);

    // 新增记录(同一商户同月已存在时会因唯一键报错, 一般用 addTotalAmount 代替)
    int add(GbLeaderMerchantMonthlyAmount info);

    // 按主键更新
    int edit(GbLeaderMerchantMonthlyAmount info);

    // 收款金额累计(不存在则插入, 存在则累加)
    int addTotalAmount(Long leaderId, Long busId, String merchantNo, String yearMonth, BigDecimal totalAmount);

}
