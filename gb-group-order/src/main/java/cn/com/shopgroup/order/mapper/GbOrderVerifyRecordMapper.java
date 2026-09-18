package cn.com.shopgroup.order.mapper;

import cn.com.shopgroup.order.model.GbOrderVerifyRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

// 订单核销记录表
@Mapper
@Repository
public interface GbOrderVerifyRecordMapper extends BaseMapper<GbOrderVerifyRecord> {
}
