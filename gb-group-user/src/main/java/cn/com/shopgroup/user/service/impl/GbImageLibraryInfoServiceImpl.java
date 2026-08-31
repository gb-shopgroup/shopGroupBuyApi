package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.user.mapper.GbImageLibraryInfoMapper;
import cn.com.shopgroup.user.model.GbImageLibraryInfo;
import cn.com.shopgroup.user.service.GbImageLibraryInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbImageLibraryInfoServiceImpl implements GbImageLibraryInfoService {

    @Resource
    private GbImageLibraryInfoMapper mapper;


    public List<GbImageLibraryInfo> getImageList(Long leaderId, int page, int pageSize){

        LambdaQueryWrapper<GbImageLibraryInfo> queryWrapper = Wrappers.lambdaQuery();
        if(leaderId > 0) queryWrapper.eq(GbImageLibraryInfo::getLeaderId, leaderId);
        queryWrapper.orderByDesc(GbImageLibraryInfo::getImgId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbImageLibraryInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Long getImageCount(Long leaderId){

        LambdaQueryWrapper<GbImageLibraryInfo> queryWrapper = Wrappers.lambdaQuery();
        if(leaderId > 0) queryWrapper.eq(GbImageLibraryInfo::getLeaderId, leaderId);
        return mapper.selectCount(queryWrapper);
    }


    public GbImageLibraryInfo getImageInfo(Long leaderId, Long imgId){

        LambdaQueryWrapper<GbImageLibraryInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbImageLibraryInfo::getImgId, imgId);
        if(leaderId > 0) queryWrapper.eq(GbImageLibraryInfo::getLeaderId, leaderId);
        queryWrapper.last("limit 0,1");
        return mapper.selectOne(queryWrapper);
    }


    public Boolean addImage(Long leaderId,Byte imgType, String imgUrl, Byte isPrivate){

        GbImageLibraryInfo record = new GbImageLibraryInfo();
        record.setImgType(imgType);
        record.setImgUrl(imgUrl);
        record.setIsPrivate(isPrivate);
        record.setLeaderId(leaderId);
        record.setAddTime(TimeUtils.getTimeStamp());
        int res = mapper.insert(record);
        return res > 0 ? true : false;
    }


    public Boolean removeImage(Long leaderId, Long imgId){

        LambdaQueryWrapper<GbImageLibraryInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbImageLibraryInfo::getImgId, imgId);
        if(leaderId > 0) queryWrapper.eq(GbImageLibraryInfo::getLeaderId, leaderId);
        int res = mapper.delete(queryWrapper);
        return res > 0 ? true : false;
    }

}
