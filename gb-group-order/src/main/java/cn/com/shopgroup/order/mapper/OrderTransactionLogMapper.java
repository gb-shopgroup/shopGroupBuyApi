package cn.com.shopgroup.order.mapper;

import cn.com.shopgroup.order.model.OrderTransactionLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface OrderTransactionLogMapper extends BaseMapper<OrderTransactionLog> {

}
