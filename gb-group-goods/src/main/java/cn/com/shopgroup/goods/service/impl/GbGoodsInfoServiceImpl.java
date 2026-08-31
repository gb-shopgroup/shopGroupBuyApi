package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.mapper.GbGoodsImageInfoMapper;
import cn.com.shopgroup.goods.mapper.GbGoodsInfoMapper;
import cn.com.shopgroup.goods.model.GbGoodsImageInfo;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GbGoodsInfoServiceImpl implements GbGoodsInfoService {

    @Resource
    private GbGoodsInfoMapper mapper;

    @Resource
    private GbGoodsImageInfoMapper imageMapper;


    public List<GbGoodsInfo> getAdminGoodsList(int page, int pageSize) {

        LambdaQueryWrapper<GbGoodsInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.orderByDesc(GbGoodsInfo::getGoodsId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbGoodsInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Long getAdminGoodsCount() {

        LambdaQueryWrapper<GbGoodsInfo> queryWrapper = Wrappers.lambdaQuery();
        return mapper.selectCount(queryWrapper);
    }


    public GbGoodsInfo getGoodsInfo(Long goodsId) {

        return mapper.selectById(goodsId);
    }


    public List<GbGoodsInfo> getGoodsStockList(List<Long> goodsIds) {

        LambdaQueryWrapper<GbGoodsInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GbGoodsInfo::getGoodsId, GbGoodsInfo::getGoodsNum);
        queryWrapper.in(GbGoodsInfo::getGoodsId, goodsIds);
        queryWrapper.orderByAsc(GbGoodsInfo::getGoodsId);
        List<GbGoodsInfo> results = mapper.selectList(queryWrapper);
        return results == null ? new ArrayList<>() : results;
    }


    public List<String> getMiniGoodsImgList(Long goodsId, Integer size) {

        LambdaQueryWrapper<GbGoodsImageInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GbGoodsImageInfo::getGoodsImg);
        queryWrapper.eq(GbGoodsImageInfo::getGoodsId, goodsId);
        queryWrapper.orderByAsc(GbGoodsImageInfo::getImgId);
        queryWrapper.last("limit 0," + size);
        List<GbGoodsImageInfo> result = imageMapper.selectList(queryWrapper);
        List<String> goodsImgs = new ArrayList<>();
        if (result != null) {
            for (GbGoodsImageInfo item : result) {
                goodsImgs.add(item.getGoodsImg());
            }
        }
        return goodsImgs;
    }


    public Integer getMiniGoodsStock(Long goodsId) {

        LambdaQueryWrapper<GbGoodsInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GbGoodsInfo::getGoodsNum);
        queryWrapper.eq(GbGoodsInfo::getGoodsId, goodsId);
        GbGoodsInfo info = mapper.selectOne(queryWrapper);
        return info.getGoodsNum();
    }


    public List<GbGoodsInfo> getMiniLeaderOnlineGoodsList(Long leaderId) {

        LambdaQueryWrapper<GbGoodsInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGoodsInfo::getIsCheck, 1);
        queryWrapper.eq(GbGoodsInfo::getIsClose, 0);
        queryWrapper.eq(GbGoodsInfo::getLeaderId, leaderId);
        queryWrapper.orderByDesc(GbGoodsInfo::getGoodsId);
        queryWrapper.last("limit 0, 100");
        List<GbGoodsInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public List<GbGoodsInfo> getMiniLeaderGoodsList(Long leaderId, Long catId, int page, int pageSize) {

        LambdaQueryWrapper<GbGoodsInfo> queryWrapper = Wrappers.lambdaQuery();
        if (catId > 0) {
            queryWrapper.eq(GbGoodsInfo::getCatId, catId);
        }
        queryWrapper.eq(GbGoodsInfo::getLeaderId, leaderId);
        queryWrapper.orderByDesc(GbGoodsInfo::getGoodsId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbGoodsInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Long getMiniLeaderGoodsCount(Long leaderId, Long catId) {

        LambdaQueryWrapper<GbGoodsInfo> queryWrapper = Wrappers.lambdaQuery();
        if (catId > 0) {
            queryWrapper.eq(GbGoodsInfo::getCatId, catId);
        }
        queryWrapper.eq(GbGoodsInfo::getLeaderId, leaderId);
        return mapper.selectCount(queryWrapper);
    }


    public Long addMiniLeaderGoodsInfo(Long leaderId, GbGoodsInfo info, List<String> imgList) {
        GbGoodsInfo data = new GbGoodsInfo();
        data.setCatId(info.getCatId());
        data.setLeaderId(leaderId);
        data.setGoodsType(info.getGoodsType());
        data.setGoodsName(info.getGoodsName());
        data.setGoodsImg(imgList.get(0));
        data.setCostPrice(info.getCostPrice());
        data.setSalesPrice(info.getSalesPrice());
        data.setMarketPrice(info.getMarketPrice());
        data.setIsStock(info.getIsStock());
        data.setGoodsNum(info.getGoodsNum());
        data.setIsLimit(info.getIsLimit());
        data.setLimitNum(info.getLimitNum());
        data.setGoodsUnit(info.getGoodsUnit());
        data.setGoodsInfo("");
        data.setIsClose((byte) 0);
        data.setIsCheck((byte) 1);
        data.setCheckRemark("");
        data.setAddTime(TimeUtils.getTimeStamp());
        mapper.insert(data);
        Long goodsId = data.getGoodsId();
        List<GbGoodsImageInfo> imageInfoList = new ArrayList<>();
        for (String img : imgList) {
            GbGoodsImageInfo temp = new GbGoodsImageInfo();
            temp.setGoodsId(goodsId);
            temp.setGoodsType((byte) 0);
            temp.setGoodsImg(img);
            imageInfoList.add(temp);
        }
        if (imageInfoList.size() > 0) imageMapper.insert(imageInfoList);
        return data.getGoodsId();
    }


    public Boolean editMiniLeaderGoodsInfo(Long leaderId, GbGoodsInfo info, List<String> imgList) {

        LambdaUpdateWrapper<GbGoodsInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbGoodsInfo::getGoodsName, info.getGoodsName());
        updateWrapper.set(GbGoodsInfo::getGoodsImg, imgList.get(0));
        updateWrapper.set(GbGoodsInfo::getCostPrice, info.getCostPrice());
        updateWrapper.set(GbGoodsInfo::getSalesPrice, info.getSalesPrice());
        updateWrapper.set(GbGoodsInfo::getMarketPrice, info.getMarketPrice());
        updateWrapper.set(GbGoodsInfo::getIsStock, info.getIsStock());
        updateWrapper.set(GbGoodsInfo::getGoodsNum, info.getGoodsNum());
        updateWrapper.set(GbGoodsInfo::getIsLimit, info.getIsLimit());
        updateWrapper.set(GbGoodsInfo::getLimitNum, info.getLimitNum());
        updateWrapper.set(GbGoodsInfo::getGoodsUnit, info.getGoodsUnit());
        updateWrapper.set(GbGoodsInfo::getIsCheck, 1);
        updateWrapper.eq(GbGoodsInfo::getGoodsId, info.getGoodsId());
        updateWrapper.eq(GbGoodsInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);
        LambdaUpdateWrapper<GbGoodsImageInfo> updateWrapper1 = Wrappers.lambdaUpdate();
        LambdaQueryWrapper<GbGoodsImageInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGoodsImageInfo::getGoodsId, info.getGoodsId());
        List<GbGoodsImageInfo> imageInfoList = imageMapper.selectList(queryWrapper);
        for (int i = 0; i < imageInfoList.size(); i++) {
            Long tempId = imageInfoList.get(i).getImgId();
            String tempImg2 = imgList.get(i);
            updateWrapper1.set(GbGoodsImageInfo::getGoodsImg, tempImg2);
            updateWrapper1.eq(GbGoodsImageInfo::getImgId, tempId);
            imageMapper.update(updateWrapper1);
        }
        return flag > 0 ? true : false;
    }

    public Boolean closeMiniLeaderGoodsInfo(Long leaderId, Long goodsId, Integer status) {
        LambdaUpdateWrapper<GbGoodsInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbGoodsInfo::getIsClose, status);
        updateWrapper.eq(GbGoodsInfo::getGoodsId, goodsId);
        updateWrapper.eq(GbGoodsInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean reduceGoodsStock(Long goodsId, Integer goodsNum) {

        LambdaUpdateWrapper<GbGoodsInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.setSql("goods_num = goods_num - {0}", Math.abs(goodsNum));
        updateWrapper.eq(GbGoodsInfo::getGoodsId, goodsId);
        updateWrapper.gt(GbGoodsInfo::getGoodsNum, 0);
        updateWrapper.eq(GbGoodsInfo::getIsStock, 1);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean increaseGoodsStock(Long goodsId, int goodsNum) {

        LambdaUpdateWrapper<GbGoodsInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.setSql("goods_num = goods_num + {0}", Math.abs(goodsNum));
        updateWrapper.eq(GbGoodsInfo::getGoodsId, goodsId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Map<Long, Integer> getGoodsStock(List<Long> goodsIdList) {
        List<GbGoodsInfo> results = getGoodsStockList(goodsIdList);
        Map<Long, Integer> goodStockData = new HashMap<>();
        for (GbGoodsInfo item : results) {
            Long gid = item.getGoodsId();
            Integer gNum = item.getGoodsNum();
            goodStockData.put(gid, gNum);
        }
        return goodStockData;
    }
}
