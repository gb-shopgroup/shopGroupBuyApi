package cn.com.shopgroup.user.mapper;

import cn.com.shopgroup.user.model.GbMemberInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface GbMemberInfoMapper extends BaseMapper<GbMemberInfo> {

}
