package cn.com.shopgroup.order.mapper;

import cn.com.shopgroup.order.model.GbReportBusinessInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface GbReportBusinessInfoMapper extends BaseMapper<GbReportBusinessInfo> {

    // 按收款账户统计每日收款情况, bus_id是 Long 类型, bus_fee是 BigDecimal 类型 ！！！
    @Select("SELECT `bus_id`, SUM(`bus_fee`) AS bus_fee FROM `gb_order_business_info` WHERE `is_divide` = 1 AND `divide_time` BETWEEN #{startTime} AND #{endTime} GROUP BY `bus_id`")
    List<Map<String, Object>> sumReportBusinessList(long startTime, long endTime);

    @Select("SELECT `leader_id` AS leaderId, `bus_name` AS busName FROM `gb_org_business_info` WHERE `bus_id` = #{busId}")
    Map<String, Object> selectOrgBusinessLeaderInfo(Long busId);

}
