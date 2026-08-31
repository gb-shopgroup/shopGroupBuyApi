package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;

import java.util.List;

public interface GbOrgStaffInfoService {


    Long addAdminLeaderStaffInfo(Long leaderId, String name, String mobile,
                                 String nickName, String avatar, String openid);


    GbOrgStaffInfo getAdminLeaderStaffInfoByMobile(String mobile);


    GbOrgStaffInfo getStaffInfo(Long id);


    GbOrgStaffInfo getMiniStaffInfo(String openid);


    List<GbOrgPointInfo> getMiniStaffPointList(Long staffId);


    List<GbOrgStaffInfo> getMiniLeaderStaffList(Long leaderId);


    Long addMiniLeaderStaff(Long leaderId, GbOrgStaffInfo info, List<Long> pointList);


    Boolean editMiniLeaderStaff(Long leaderId, GbOrgStaffInfo info, List<Long> pointList);


    Boolean closeMiniLeaderStaff(Long leaderId, Long staffId, Integer status);

    /**
     * 删除员工
     *
     * @param staffId
     * @return
     */
    int removeStaff(Long leaderId, Long staffId);

}
