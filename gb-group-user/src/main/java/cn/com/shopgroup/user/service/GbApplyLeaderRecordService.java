package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.model.GbApplyLeaderRecord;

public interface GbApplyLeaderRecordService {

    // 新增申请团长记录(返回记录id)
    Long addApplyLeaderRecord(GbApplyLeaderRecord info);

    // 根据手机号查询申请记录(唯一键查重用)
    GbApplyLeaderRecord getApplyLeaderRecordByMobile(String mobile);

}
