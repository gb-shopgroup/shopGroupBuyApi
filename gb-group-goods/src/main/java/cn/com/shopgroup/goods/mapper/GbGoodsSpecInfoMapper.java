package cn.com.shopgroup.goods.mapper;

import cn.com.shopgroup.goods.model.GbGoodsSpecInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface GbGoodsSpecInfoMapper extends BaseMapper<GbGoodsSpecInfo> {

    // 按规格id列表更新商品id
    int updateGoodsIdByIds(@Param("leaderId") Long leaderId, @Param("goodsId") Long goodsId, @Param("specInfoIds") List<Long> specInfoIds);
}
