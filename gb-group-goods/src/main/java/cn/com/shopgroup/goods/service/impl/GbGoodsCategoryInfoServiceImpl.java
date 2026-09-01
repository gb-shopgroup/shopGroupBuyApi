package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.goods.mapper.GbGoodsCategoryInfoMapper;
import cn.com.shopgroup.goods.model.GbGoodsCategoryInfo;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.service.GbGoodsCategoryInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public Map<Long, String> getGoodsCategoryNameMap() {
        LambdaQueryWrapper<GbGoodsCategoryInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GbGoodsCategoryInfo::getCatId, GbGoodsCategoryInfo::getCatName);
        List<GbGoodsCategoryInfo> list = goodsCategoryInfoMapper.selectList(queryWrapper);
        Map<Long, String> map = new HashMap<>();
        if (list != null) {
            for (GbGoodsCategoryInfo item : list) {
                map.put(item.getCatId(), item.getCatName());
            }
        }
        return map;
    }
}
