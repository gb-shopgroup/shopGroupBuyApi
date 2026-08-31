package cn.com.shopgroup.order.service;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.order.mapper.GbReportBusinessInfoMapper;
import cn.com.shopgroup.order.model.GbReportBusinessInfo;
import cn.com.shopgroup.user.mapper.GbOrgBusinessInfoMapper;
import cn.com.shopgroup.user.model.GbOrgBusinessInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.executor.BatchResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GbReportBusinessInfoService {

    @Resource
    private GbReportBusinessInfoMapper mapper;

    @Resource
    private GbOrgBusinessInfoMapper businessMapper;


    public List<GbReportBusinessInfo> getAdminReportBusinessList(int page, int pageSize) {

        LambdaQueryWrapper<GbReportBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.orderByDesc(GbReportBusinessInfo::getReportId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbReportBusinessInfo> lists = mapper.selectList(queryWrapper);
        return lists == null ? new ArrayList<>() : lists;
    }


    public long getAdminReportBusinessCount() {

        LambdaQueryWrapper<GbReportBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        return mapper.selectCount(queryWrapper);
    }


    public List<GbReportBusinessInfo> getMiniLeaderReportBusinessList(Long leaderId, int page, int pageSize) {

        LambdaQueryWrapper<GbReportBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbReportBusinessInfo::getLeaderId, leaderId);
        queryWrapper.orderByDesc(GbReportBusinessInfo::getReportId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbReportBusinessInfo> lists = mapper.selectList(queryWrapper);
        return lists == null ? new ArrayList<>() : lists;
    }


    public long getMiniLeaderReportBusinessCount(Long leaderId) {

        LambdaQueryWrapper<GbReportBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbReportBusinessInfo::getLeaderId, leaderId);
        return mapper.selectCount(queryWrapper);
    }


    public List<GbReportBusinessInfo> sumReportBusinessList(long startTime, long endTime) {

        List<GbReportBusinessInfo> data = new ArrayList<>();
        List<Map<String, Object>> results = mapper.sumReportBusinessList(startTime, endTime);
        for (Map<String, Object> item : results) {
            Long busId = (Long) item.get("bus_id");
            Long busFee = ((BigDecimal) item.get("bus_fee")).longValue();
            GbReportBusinessInfo temp = new GbReportBusinessInfo();
            temp.setBusId(busId);
            temp.setBusFee(busFee.intValue());
            data.add(temp);
        }
        return data;
    }


    public int addMiniLeaderReportBusiness(List<GbReportBusinessInfo> data) {


        int nowTime = TimeUtils.getTimeStamp();
        for (GbReportBusinessInfo item : data) {
            Long busId = item.getBusId();
            GbOrgBusinessInfo info = businessMapper.selectById(busId);
            item.setLeaderId(info.getLeaderId());
            item.setBusName(info.getBusName());
            item.setAddTime(nowTime);
        }


        List<BatchResult> results = mapper.insert(data);
        return results.size();
    }


}
