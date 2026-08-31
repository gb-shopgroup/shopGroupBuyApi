package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbFocusInfoMapper;
import cn.com.shopgroup.user.model.GbFocusInfo;
import cn.com.shopgroup.user.service.GbFocusInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbFocusInfoServiceImpl implements GbFocusInfoService {

    @Resource
    private GbFocusInfoMapper mapper;


    public List<GbFocusInfo> getMiniFocusList() {

        LambdaQueryWrapper<GbFocusInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(
                GbFocusInfo::getFocusId,
                GbFocusInfo::getFocusTitle,
                GbFocusInfo::getFocusImg,
                GbFocusInfo::getFocusLink);
        queryWrapper.eq(GbFocusInfo::getIsClose, 0);
        queryWrapper.orderByAsc(GbFocusInfo::getSortOrder);
        queryWrapper.orderByAsc(GbFocusInfo::getFocusId);
        List<GbFocusInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


}
