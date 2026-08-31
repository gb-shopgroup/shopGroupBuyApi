package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.model.GbOrgMessageInfo;

import java.util.List;

public interface GbOrgMessageInfoService {

    List<GbOrgMessageInfo> getMiniLeaderMessageList(Long leaderId, Long staffId, Integer msgType,
                                                    int page, int pageSize);


    Long getMiniLeaderMessageCount(Long leaderId, Long staffId, Integer msgType);


    Long getMiniLeaderMessageUnReadCount(Long leaderId, Long staffId, Integer msgType);


    Long addMiniLeaderMessageInfo(Long leaderId, Long staffId, Byte type, String msg);


    Boolean readMiniLeaderMessageInfo(Long leaderId, Long staffId, Long msgId);


}
