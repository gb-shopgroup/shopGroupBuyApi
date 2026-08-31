package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.mapper.GbGoodsSkuInfoMapper;
import cn.com.shopgroup.goods.mapper.GbGoodsSpecInfoMapper;
import cn.com.shopgroup.goods.mapper.GbGoodsSpecValueMapper;
import cn.com.shopgroup.goods.model.GbGoodsSkuInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecValue;
import cn.com.shopgroup.goods.service.GbGoodsSpecInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GbGoodsSpecInfoServiceImpl implements GbGoodsSpecInfoService {

    @Resource
    private GbGoodsSpecInfoMapper mapper;
    @Resource
    private GbGoodsSpecValueMapper valueMapper;
    @Resource
    private GbGoodsSkuInfoMapper skuMapper;

    public GbGoodsSpecInfo getGoodsSpecInfo(Long specId) {
        return mapper.selectById(specId);
    }

    public List<GbGoodsSpecInfo> getMiniGoodsSpecList(Long goodsId) {
        LambdaQueryWrapper<GbGoodsSpecInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GbGoodsSpecInfo::getSpecId, GbGoodsSpecInfo::getSpecName);
        queryWrapper.eq(GbGoodsSpecInfo::getIsClose, 0);
        queryWrapper.orderByAsc(GbGoodsSpecInfo::getSpecId);
        List<GbGoodsSpecInfo> data = mapper.selectList(queryWrapper);
        if (data == null || data.size() == 0) return new ArrayList<>();


        List<Long> specIds = new ArrayList<>();
        for (GbGoodsSpecInfo item : data) {
            specIds.add(item.getSpecId());
        }
        LambdaQueryWrapper<GbGoodsSpecValue> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.select(
                GbGoodsSpecValue::getSpecId,
                GbGoodsSpecValue::getValId,
                GbGoodsSpecValue::getSpecVal);
        queryWrapper2.in(GbGoodsSpecValue::getSpecId, specIds);
        queryWrapper2.orderByAsc(GbGoodsSpecValue::getValId); // 这个排序非常重要
        List<GbGoodsSpecValue> specValueList = valueMapper.selectList(queryWrapper2);


        Map<Long, List<GbGoodsSpecValue>> specValueMap = new HashMap<>();
        for (GbGoodsSpecValue item : specValueList) {
            Long tempId = item.getSpecId();
            if (specValueMap.containsKey(tempId)) {
                specValueMap.get(tempId).add(item);
            } else {
                List<GbGoodsSpecValue> temp = new ArrayList<>();
                temp.add(item);
                specValueMap.put(tempId, temp);
            }
        }


        for (GbGoodsSpecInfo item : data) {
            Long tempId = item.getSpecId();
            if (specValueMap.containsKey(tempId)) {
                item.setSpecValueList(specValueMap.get(tempId));
            } else {
                item.setSpecValueList(new ArrayList<>());
            }
        }


        return data;
    }


    public List<GbGoodsSpecInfo> getMiniLeaderGoodsSpecList(Long goodsId) {


        LambdaQueryWrapper<GbGoodsSpecInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.orderByAsc(GbGoodsSpecInfo::getSpecId);
        List<GbGoodsSpecInfo> result = mapper.selectList(queryWrapper);
        if (result == null || result.size() == 0) return result;


        List<Long> specIds = new ArrayList<>();
        for (GbGoodsSpecInfo item : result) {
            specIds.add(item.getSpecId());
        }


        LambdaQueryWrapper<GbGoodsSpecValue> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.in(GbGoodsSpecValue::getSpecId, specIds);
        queryWrapper2.orderByAsc(GbGoodsSpecValue::getValId);
        List<GbGoodsSpecValue> valList = valueMapper.selectList(queryWrapper2);


        Map<Long, List<GbGoodsSpecValue>> valMap = new HashMap<>();
        for (GbGoodsSpecValue item : valList) {
            Long tempId = item.getSpecId();
            if (valMap.containsKey(tempId)) {
                valMap.get(tempId).add(item);
            } else {
                List<GbGoodsSpecValue> tempList = new ArrayList<>();
                tempList.add(item);
                valMap.put(tempId, tempList);
            }
        }


        for (GbGoodsSpecInfo item : result) {

            Long tempId = item.getSpecId();
            if (valMap.containsKey(tempId)) {
                item.setSpecValueList(valMap.get(tempId));
            } else {
                item.setSpecValueList(new ArrayList<>());
            }
        }


        return result == null ? new ArrayList<>() : result;
    }


    public Long addMiniLeaderGoodsSpec(GbGoodsSpecInfo info) {

        GbGoodsSpecInfo data = new GbGoodsSpecInfo();

        data.setSpecName(info.getSpecName());

        data.setIsPrice(info.getIsPrice());

        data.setIsStock(info.getIsStock());

        data.setSortOrder(255);

        data.setIsClose((byte) 0);

        data.setAddTime(TimeUtils.getTimeStamp());


        mapper.insert(data);
        return data.getSpecId();
    }


    public Boolean editMiniLeaderGoodsSpec(GbGoodsSpecInfo info) {

        LambdaUpdateWrapper<GbGoodsSpecInfo> updateWrapper = Wrappers.lambdaUpdate();

        updateWrapper.set(GbGoodsSpecInfo::getSpecName, info.getSpecName());

        updateWrapper.set(GbGoodsSpecInfo::getIsPrice, info.getIsPrice());

        updateWrapper.set(GbGoodsSpecInfo::getIsStock, info.getIsStock());

        updateWrapper.eq(GbGoodsSpecInfo::getSpecId, info.getSpecId());

        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean closeMiniLeaderGoodsSpec(Long specId, int status) {

        LambdaUpdateWrapper<GbGoodsSpecInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbGoodsSpecInfo::getIsClose, status);
        updateWrapper.eq(GbGoodsSpecInfo::getSpecId, specId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean removeMiniLeaderGoodsSpec(Long specId) {

        int flag = mapper.deleteById(specId);
        return flag > 0 ? true : false;
    }


    public Boolean closeMiniLeaderGoodsSpecVal(int valId, int status) {

        LambdaUpdateWrapper<GbGoodsSpecValue> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbGoodsSpecValue::getIsClose, status);
        updateWrapper.eq(GbGoodsSpecValue::getValId, valId);
        int flag = valueMapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }

    public Boolean updateMiniLeaderGoodsSpecVal(Long leaderId, Long goodsId, List<GbGoodsSpecInfo> lists) {

        LambdaQueryWrapper<GbGoodsSkuInfo> queryWrapper1 = Wrappers.lambdaQuery();
        queryWrapper1.eq(GbGoodsSkuInfo::getGoodsId, goodsId);
        queryWrapper1.eq(GbGoodsSkuInfo::getLeaderId, leaderId);
        skuMapper.delete(queryWrapper1);

        LambdaQueryWrapper<GbGoodsSpecValue> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.eq(GbGoodsSpecValue::getLeaderId, leaderId);
        valueMapper.delete(queryWrapper2);

        LambdaQueryWrapper<GbGoodsSpecInfo> queryWrapper3 = Wrappers.lambdaQuery();
        queryWrapper3.eq(GbGoodsSpecInfo::getLeaderId, leaderId);
        mapper.delete(queryWrapper3);
        int nowTime = TimeUtils.getTimeStamp();
        for (GbGoodsSpecInfo item : lists) {
            item.setLeaderId(leaderId);
            item.setIsPrice((byte) 1);
            item.setIsStock((byte) 1);
            item.setSortOrder(255);
            item.setIsClose((byte) 0);
            item.setAddTime(nowTime);
            mapper.insert(item);
            Long specId = item.getSpecId();
            List<GbGoodsSpecValue> valueList = item.getSpecValueList();
            for (GbGoodsSpecValue temp : valueList) {

                temp.setLeaderId(leaderId);

                temp.setSpecId(specId);

                temp.setIsClose((byte) 0);

                temp.setAddTime(nowTime);
            }
            valueMapper.insert(valueList);
        }

        return true;
    }

    @Override
    public List<GbGoodsSpecInfo> getLeaderGoodsSpecList(Long leaderId) {
        LambdaQueryWrapper<GbGoodsSpecInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGoodsSpecInfo::getLeaderId, leaderId);
        queryWrapper.orderByAsc(GbGoodsSpecInfo::getSpecId);
        List<GbGoodsSpecInfo> result = mapper.selectList(queryWrapper);
        if (result == null || result.size() == 0) return result;


        List<Long> specIds = new ArrayList<>();
        for (GbGoodsSpecInfo item : result) {
            specIds.add(item.getSpecId());
        }
        LambdaQueryWrapper<GbGoodsSpecValue> queryWrapper2 = Wrappers.lambdaQuery();
        queryWrapper2.in(GbGoodsSpecValue::getSpecId, specIds);
        queryWrapper2.orderByAsc(GbGoodsSpecValue::getValId);
        List<GbGoodsSpecValue> valList = valueMapper.selectList(queryWrapper2);
        Map<Long, List<GbGoodsSpecValue>> valMap = new HashMap<>();
        for (GbGoodsSpecValue item : valList) {
            Long tempId = item.getSpecId();
            if (valMap.containsKey(tempId)) {
                valMap.get(tempId).add(item);
            } else {
                List<GbGoodsSpecValue> tempList = new ArrayList<>();
                tempList.add(item);
                valMap.put(tempId, tempList);
            }
        }

        for (GbGoodsSpecInfo item : result) {
            Long tempId = item.getSpecId();
            if (valMap.containsKey(tempId)) {
                item.setSpecValueList(valMap.get(tempId));
            } else {
                item.setSpecValueList(new ArrayList<>());
            }
        }
        return result == null ? new ArrayList<>() : result;
    }

    @Override
    public int updateGoodsIdByIds(Long leaderId, Long goodsId, List<Long> specInfoIds) {
        if (CollectionUtils.isEmpty(specInfoIds)) {
            return 0;
        }
        return valueMapper.updateGoodsIdByIds(leaderId, goodsId, specInfoIds);
    }

    @Override
    public List<GbGoodsSpecInfo> getGoodsSpecListByGoodsId(Long goodsId) {
        if (goodsId == null) {
            return new ArrayList<>();
        }
        List<Long> goodsIds = new ArrayList<>();
        goodsIds.add(goodsId);
        Map<Long, List<GbGoodsSpecInfo>> map = getGoodsSpecListByGoodsIds(goodsIds);
        return map.getOrDefault(goodsId, new ArrayList<>());
    }

    @Override
    public Map<Long, List<GbGoodsSpecInfo>> getGoodsSpecListByGoodsIds(List<Long> goodsIds) {
        Map<Long, List<GbGoodsSpecInfo>> result = new HashMap<>();
        if (CollectionUtils.isEmpty(goodsIds)) {
            return result;
        }
        // 1. 查询这些商品关联的规格值(规格值表通过 goods_id 与商品关联)
        LambdaQueryWrapper<GbGoodsSpecValue> valWrapper = Wrappers.lambdaQuery();
        valWrapper.select(GbGoodsSpecValue::getGoodsId, GbGoodsSpecValue::getSpecId,
                GbGoodsSpecValue::getValId, GbGoodsSpecValue::getSpecVal);
        valWrapper.in(GbGoodsSpecValue::getGoodsId, goodsIds);
        valWrapper.eq(GbGoodsSpecValue::getIsClose, 0);
        valWrapper.orderByAsc(GbGoodsSpecValue::getValId);
        List<GbGoodsSpecValue> valList = valueMapper.selectList(valWrapper);
        if (CollectionUtils.isEmpty(valList)) {
            return result;
        }
        // 2. 提取去重后的规格id
        Set<Long> specIdSet = new LinkedHashSet<>();
        for (GbGoodsSpecValue item : valList) {
            specIdSet.add(item.getSpecId());
        }
        // 3. 查询规格信息
        LambdaQueryWrapper<GbGoodsSpecInfo> specWrapper = Wrappers.lambdaQuery();
        specWrapper.select(GbGoodsSpecInfo::getSpecId, GbGoodsSpecInfo::getSpecName);
        specWrapper.in(GbGoodsSpecInfo::getSpecId, specIdSet);
        specWrapper.eq(GbGoodsSpecInfo::getIsClose, 0);
        specWrapper.orderByAsc(GbGoodsSpecInfo::getSpecId);
        List<GbGoodsSpecInfo> specList = mapper.selectList(specWrapper);
        if (CollectionUtils.isEmpty(specList)) {
            return result;
        }
        // 4. 按 商品id -> 规格id -> 规格值列表 三级分组
        Map<Long, Map<Long, List<GbGoodsSpecValue>>> goodsSpecValMap = new HashMap<>();
        for (GbGoodsSpecValue item : valList) {
            Long tempGoodsId = item.getGoodsId();
            if (!goodsSpecValMap.containsKey(tempGoodsId)) {
                goodsSpecValMap.put(tempGoodsId, new HashMap<>());
            }
            Long tempSpecId = item.getSpecId();
            if (!goodsSpecValMap.get(tempGoodsId).containsKey(tempSpecId)) {
                goodsSpecValMap.get(tempGoodsId).put(tempSpecId, new ArrayList<>());
            }
            goodsSpecValMap.get(tempGoodsId).get(tempSpecId).add(item);
        }
        // 5. 组装每个商品下的规格(独立对象, 避免引用共享)
        for (Map.Entry<Long, Map<Long, List<GbGoodsSpecValue>>> entry : goodsSpecValMap.entrySet()) {
            Long tempGoodsId = entry.getKey();
            List<GbGoodsSpecInfo> specs = new ArrayList<>();
            for (GbGoodsSpecInfo spec : specList) {
                List<GbGoodsSpecValue> values = entry.getValue().get(spec.getSpecId());
                if (values != null) {
                    GbGoodsSpecInfo copy = new GbGoodsSpecInfo();
                    copy.setSpecId(spec.getSpecId());
                    copy.setSpecName(spec.getSpecName());
                    copy.setSpecValueList(values);
                    specs.add(copy);
                }
            }
            result.put(tempGoodsId, specs);
        }
        return result;
    }


}
