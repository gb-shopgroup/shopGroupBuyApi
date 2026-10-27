package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbApplyLeaderRecordMapper;
import cn.com.shopgroup.user.model.GbApplyLeaderRecord;
import cn.com.shopgroup.user.service.GbApplyLeaderRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class GbApplyLeaderRecordServiceImpl implements GbApplyLeaderRecordService {

    @Resource
    private GbApplyLeaderRecordMapper mapper;

    // 新增申请团长记录: 默认申请中(0), 时间由应用层写入(表默认值仅作为兜底)
    public Long addApplyLeaderRecord(GbApplyLeaderRecord info) {

        GbApplyLeaderRecord data = new GbApplyLeaderRecord();
        data.setName(info.getName());
        data.setMobile(info.getMobile());
        // 未指定申请状态时默认 0=申请中
        data.setApplyStatus(info.getApplyStatus() == null ? 0 : info.getApplyStatus());
        data.setAddTime(new Date());
        data.setUpdateTime(new Date());
        data.setMemberId(info.getMemberId());
        mapper.insert(data);
        return data.getId();
    }

    // 根据手机号查询申请记录(手机号唯一键, 取最新一条)
    public GbApplyLeaderRecord getApplyLeaderRecordByMobile(String mobile) {

        LambdaQueryWrapper<GbApplyLeaderRecord> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbApplyLeaderRecord::getMobile, mobile);
        queryWrapper.orderByDesc(GbApplyLeaderRecord::getId);
        queryWrapper.last("limit 0, 1");
        return mapper.selectOne(queryWrapper);
    }

}
