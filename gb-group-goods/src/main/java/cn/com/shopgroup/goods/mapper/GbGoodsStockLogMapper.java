package cn.com.shopgroup.goods.mapper;

import cn.com.shopgroup.goods.model.GbGoodsStockLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface GbGoodsStockLogMapper extends BaseMapper<GbGoodsStockLog> {

}
