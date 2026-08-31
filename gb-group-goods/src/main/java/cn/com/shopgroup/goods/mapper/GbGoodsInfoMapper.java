package cn.com.shopgroup.goods.mapper;

import cn.com.shopgroup.goods.model.GbGoodsInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface GbGoodsInfoMapper extends BaseMapper<GbGoodsInfo> {

}
