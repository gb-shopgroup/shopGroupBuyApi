package cn.com.shopgroup.user.mapper;

import cn.com.shopgroup.user.model.GbOrgShopInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface GbOrgShopInfoMapper extends BaseMapper<GbOrgShopInfo> {

    GbOrgShopInfo getMiniLeaderShop(@Param("leaderId") Long leaderId);
}
