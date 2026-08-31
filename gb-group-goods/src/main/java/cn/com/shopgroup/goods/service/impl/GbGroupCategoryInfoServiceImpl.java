package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.goods.mapper.GbGroupCategoryInfoMapper;
import cn.com.shopgroup.goods.model.GbGroupCategoryInfo;
import cn.com.shopgroup.goods.service.GbGroupCategoryInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GbGroupCategoryInfoServiceImpl implements GbGroupCategoryInfoService {

    @Autowired
    private GbGroupCategoryInfoMapper mapper;


    public List<GbGroupCategoryInfo> getMiniGroupCategoryList(){

        LambdaQueryWrapper<GbGroupCategoryInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GbGroupCategoryInfo::getCatId,GbGroupCategoryInfo::getCatName);
        queryWrapper.eq(GbGroupCategoryInfo::getIsClose, 0);
        queryWrapper.orderByAsc(GbGroupCategoryInfo::getSortOrder);
        queryWrapper.orderByAsc(GbGroupCategoryInfo::getCatId);
        List<GbGroupCategoryInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

}
