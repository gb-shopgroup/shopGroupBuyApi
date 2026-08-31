package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.model.GbMemberAddressInfo;

import java.util.List;

public interface GbMemberAddressInfoService {

    GbMemberAddressInfo getMemberAddressInfo(int addressId);


    List<GbMemberAddressInfo> getMemberAddressList(Long memberId);


    Long addMemberAddress(GbMemberAddressInfo data);
}
