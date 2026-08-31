package cn.com.shopgroup.user.mapper;

import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.model.GbOrgPointStaff;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface GbOrgPointStaffMapper extends BaseMapper<GbOrgPointStaff> {

    // 连表查询用户的订单详情
    @Select("SELECT p.* FROM `gb_org_point_staff` AS ps " +
            "JOIN `gb_org_point_info` AS p ON p.`point_id` = ps.`point_id` " +
            "WHERE ps.`staff_id` = #{staffId} ORDER BY ps.`point_id` ASC LIMIT 0, 10")
    List<GbOrgPointInfo> getStaffPointList(Long staffId);

}
