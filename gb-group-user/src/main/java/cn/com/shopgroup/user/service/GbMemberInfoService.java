package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.model.GbMemberInfo;

import java.util.List;

public interface GbMemberInfoService {

   List<GbMemberInfo> getAdminMemberList(int page, int pageSize);


   Long getAdminMemberCount();

   GbMemberInfo getMemberInfo(Long memberId);


   GbMemberInfo getMemberInfoByMobile(String mobile) ;

   Long addMiniMember(GbMemberInfo data) ;


   GbMemberInfo getMiniMemberById(Long memberId);

   GbMemberInfo getMiniMemberByOpenId(String openId) ;

   Boolean updateMemberErcode(Long memberId, String ercode) ;
}
