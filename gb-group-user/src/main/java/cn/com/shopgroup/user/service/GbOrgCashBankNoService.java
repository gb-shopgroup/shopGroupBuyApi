package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.model.GbOrgCashBankNo;

import java.util.List;

public interface GbOrgCashBankNoService {

    Long addMiniLeaderBankNo(Long leaderId, String bankNo, String bankName, String trueName);


    Boolean isMiniLeaderBankNoExist(Long leaderId, String bankNo);


    List<GbOrgCashBankNo> getMiniLeaderBankNoList(Long leaderId);


}
