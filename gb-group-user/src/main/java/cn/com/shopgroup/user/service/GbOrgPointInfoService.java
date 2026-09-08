package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.http.response.PointResponse;
import cn.com.shopgroup.user.model.GbOrgPointInfo;

import java.util.Collection;
import java.util.List;

public interface GbOrgPointInfoService {

    GbOrgPointInfo getPointInfo(Long pointId);


    List<GbOrgPointInfo> getMiniPointList(Long leaderId);

    List<GbOrgPointInfo> getMiniLeaderPointList(Long leaderId);


    Long addMiniLeaderPoint(Long leaderId, GbOrgPointInfo info);


    boolean editMiniLeaderPoint(Long leaderId, GbOrgPointInfo info);


    boolean closeMiniLeaderPoint(Long leaderId, Long pointId, int status);


    boolean updatePointErcode(Long pointId, String ercode);


    List<PointResponse> getGroupPoint(Long leaderId);

    List<GbOrgPointInfo> getPointListForLeader(Long leaderId, String name);

    // 按自提点id批量查询自提点信息
    List<GbOrgPointInfo> getPointListByIds(Collection<Long> pointIds);
}
