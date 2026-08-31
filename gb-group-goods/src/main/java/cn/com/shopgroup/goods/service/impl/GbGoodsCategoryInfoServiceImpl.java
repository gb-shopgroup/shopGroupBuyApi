package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.goods.mapper.GbGoodsCategoryInfoMapper;
import cn.com.shopgroup.goods.model.GbGoodsCategoryInfo;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.service.GbGoodsCategoryInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class GbGoodsCategoryInfoServiceImpl implements GbGoodsCategoryInfoService {

    @Resource
    private GbGoodsCategoryInfoMapper goodsCategoryInfoMapper;

    @Override
    public List<GbGoodsCategoryInfo> getGoodsCategoryList() {
        LambdaQueryWrapper<GbGoodsCategoryInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGoodsCategoryInfo::getIsClose,0);
        queryWrapper.orderByAsc(GbGoodsCategoryInfo::getSortOrder);
        return goodsCategoryInfoMapper.selectList(queryWrapper);
    }
}
