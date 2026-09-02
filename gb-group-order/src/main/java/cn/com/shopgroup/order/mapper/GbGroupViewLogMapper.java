package cn.com.shopgroup.order.mapper;

import cn.com.shopgroup.order.model.GbGroupViewLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface GbGroupViewLogMapper extends BaseMapper<GbGroupViewLog> {

    /**
     * 团长端-统计指定成员(可多个)在团长下的查看次数, key: memberId, value: count
     */
    List<Map<String, Object>> countViewsByMembers(@Param("leaderId") Long leaderId,
                                                  @Param("memberIds") List<Long> memberIds);

    /**
     * 团长端-查询指定成员最近一条查看记录(含团购名称), 用于列表"最近动态"展示
     */
    List<Map<String, Object>> getLastViewByMembers(@Param("leaderId") Long leaderId,
                                                   @Param("memberIds") List<Long> memberIds);
}
