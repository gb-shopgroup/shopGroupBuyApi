package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.model.GbMemberBlackList;

import java.util.List;

public interface GbMemberBlackListService {


    Boolean addMemberBlack(Long leaderId, GbMemberBlackList info);

    Boolean removeMemberBlack(Long leaderId, Long memberId);

    Boolean getMemberBlackById(Long leaderId, Long memberId);

    GbMemberBlackList getMemberBlackByMobile(Long leaderId, String mobile);

    List<GbMemberBlackList> getMemberBlackList(Long leaderId, int page, int pageSize);

    Long getMemberBlackCount(Long leaderId);


}
