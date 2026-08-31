package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.mapper.GbGoodsSpecValueMapper;
import cn.com.shopgroup.goods.model.GbGoodsSpecValue;
import cn.com.shopgroup.goods.service.GbGoodsSpecValueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: liJing
 * @date: 2026/8/26 19:59
 * @version: 1.0.0
 */
@Service
public class GbGoodsSpecValueServiceImpl implements GbGoodsSpecValueService {

    @Resource
    private GbGoodsSpecValueMapper specValueMapper;

    @Override
    public Long addGoodsSpecVal(GbGoodsSpecValue info) {
        GbGoodsSpecValue data = new GbGoodsSpecValue();

        data.setSpecId(info.getSpecId());

        data.setSpecVal(info.getSpecVal());

        data.setIsClose((byte) 0);

        data.setAddTime(TimeUtils.getTimeStamp());
        specValueMapper.insert(data);
        return data.getValId();
    }

    @Override
    public int editGoodsSpecVal(GbGoodsSpecValue info) {
        LambdaUpdateWrapper<GbGoodsSpecValue> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbGoodsSpecValue::getSpecVal, info.getSpecVal());
        updateWrapper.eq(GbGoodsSpecValue::getValId, info.getValId());
        return specValueMapper.update(updateWrapper);
    }

    @Override
    public int removeMiniLeaderGoodsSpecVal(Long valId) {
        int flag = specValueMapper.deleteById(valId);
        return flag;
    }

    @Override
    public Boolean removeMiniLeaderGoodsSpecValList(Long specId) {
        LambdaQueryWrapper<GbGoodsSpecValue> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGoodsSpecValue::getSpecId, specId);
        int flag = specValueMapper.delete(queryWrapper);
        return flag > 0 ? true : false;
    }


    public List<GbGoodsSpecValue> getMiniLeaderGoodsSpecValList(Long specId) {

        LambdaQueryWrapper<GbGoodsSpecValue> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGoodsSpecValue::getSpecId, specId);
        queryWrapper.orderByAsc(GbGoodsSpecValue::getValId);
        List<GbGoodsSpecValue> result = specValueMapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    public GbGoodsSpecValue getGoodsSpecValInfo(Long valId) {

        return specValueMapper.selectById(valId);
    }

    @Override
    public int updateGoodsIdByIds(Long leaderId, Long goodsId, List<Long> specValIds) {
        if (CollectionUtils.isEmpty(specValIds)) {
            return 0;
        }
        return specValueMapper.updateGoodsIdByIds(leaderId, goodsId, specValIds);
    }
}
