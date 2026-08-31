package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.http.response.PointResponse;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.model.GbOrgPointStaff;

import java.util.List;

public interface GbOrgPointInfoService {

    GbOrgPointInfo getPointInfo(Long pointId);


    List<GbOrgPointInfo> getMiniPointList(Long leaderId);


    List<GbOrgPointStaff> getMiniPointStaffIds(Long pointId);

    List<GbOrgPointInfo> getMiniLeaderPointList(Long leaderId);


    Long addMiniLeaderPoint(Long leaderId, GbOrgPointInfo info);


    boolean editMiniLeaderPoint(Long leaderId, GbOrgPointInfo info);


    boolean closeMiniLeaderPoint(Long leaderId, Long pointId, int status);


    boolean updatePointErcode(Long pointId, String ercode);


    List<PointResponse> getGroupPoint(Long leaderId);
}
