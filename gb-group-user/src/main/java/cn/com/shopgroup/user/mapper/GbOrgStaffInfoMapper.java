package cn.com.shopgroup.user.mapper;

import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface GbOrgStaffInfoMapper extends BaseMapper<GbOrgStaffInfo> {
    /**
     * 删除员工
     *
     * @param leaderId
     * @param staffId
     * @return
     */
    int removeStaff(@Param("leaderId") Long leaderId, @Param("staffId") Long staffId);
}
