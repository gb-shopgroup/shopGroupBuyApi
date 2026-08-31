package cn.com.shopgroup.goods.mapper;

import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGroupActivityGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface GbGroupActivityGoodsMapper extends BaseMapper<GbGroupActivityGoods> {


    @Select("SELECT g.* FROM `gb_group_activity_goods` AS a JOIN `gb_goods_info` AS g ON g.`goods_id` = a.`goods_id` WHERE a.`group_id` = #{groupId} ")
    List<GbGoodsInfo> getGroupGoodsList(Long groupId);


    @Delete("DELETE FROM `gb_group_activity_goods` WHERE `group_id` = #{groupId}")
    int deleteGroupGoodsList(Long groupId);


    @Select("SELECT ag.* FROM `gb_group_activity_goods` AS ag JOIN `gb_group_activity_info` AS ai ON ag.`group_id` = ai.`group_id` AND ai.`is_check` = 1 AND ai.`is_close` = 0 AND ai.leader_id = #{leaderId} WHERE ag.`goods_id` = #{goodsId} ORDER BY ai.`group_id` DESC LIMIT 0, 1")
    GbGroupActivityGoods isGroupGoodsOnline(Long leaderId, Long goodsId);



}
