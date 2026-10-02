package cn.com.shopgroup.order.mapper;

import cn.com.shopgroup.order.model.GbLeaderMerchantMonthlyAmount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Mapper
@Repository
public interface GbLeaderMerchantMonthlyAmountMapper extends BaseMapper<GbLeaderMerchantMonthlyAmount> {

    // 收款金额累计入库: (merchant_no, year_month) 唯一键冲突时按增量累加, 无冲突时插入
    int upsertTotalAmount(@Param("leaderId") Long leaderId,
                          @Param("busId") Long busId,
                          @Param("merchantNo") String merchantNo,
                          @Param("yearMonth") String yearMonth,
                          @Param("totalAmount") BigDecimal totalAmount);

}
