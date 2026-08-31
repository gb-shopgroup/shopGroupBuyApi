package cn.com.shopgroup.goods.mapper;

import cn.com.shopgroup.goods.model.GbGoodsSpecValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface GbGoodsSpecValueMapper extends BaseMapper<GbGoodsSpecValue> {

    // 按规格值id列表更新商品id
    int updateGoodsIdByIds(@Param("leaderId") Long leaderId, @Param("goodsId") Long goodsId, @Param("specValIds") List<Long> specValIds);
}
