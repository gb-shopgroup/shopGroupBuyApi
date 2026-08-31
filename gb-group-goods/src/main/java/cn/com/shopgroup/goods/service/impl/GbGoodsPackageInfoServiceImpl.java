package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.mapper.GbGoodsPackageInfoMapper;
import cn.com.shopgroup.goods.model.GbGoodsPackageInfo;
import cn.com.shopgroup.goods.service.GbGoodsPackageInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbGoodsPackageInfoServiceImpl implements GbGoodsPackageInfoService {

    @Resource
    private GbGoodsPackageInfoMapper mapper;

    public GbGoodsPackageInfo getGoodsPackageInfo(Long packId){

        return mapper.selectById(packId);
    }

    public List<GbGoodsPackageInfo> getMiniGoodsPackageList(Long leaderId, Long goodsId){

        LambdaQueryWrapper<GbGoodsPackageInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(
                GbGoodsPackageInfo::getPackId,
                GbGoodsPackageInfo::getPackName,
                GbGoodsPackageInfo::getSalesPrice);
        queryWrapper.eq(GbGoodsPackageInfo::getIsClose, 0);
        queryWrapper.eq(GbGoodsPackageInfo::getGoodsId, goodsId);
        queryWrapper.eq(GbGoodsPackageInfo::getLeaderId, leaderId);
        queryWrapper.orderByAsc(GbGoodsPackageInfo::getPackId);
        List<GbGoodsPackageInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public List<GbGoodsPackageInfo> getMiniLeaderGoodsPackageList(Long leaderId, Long goodsId){

        LambdaQueryWrapper<GbGoodsPackageInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGoodsPackageInfo::getGoodsId, goodsId);
        queryWrapper.eq(GbGoodsPackageInfo::getLeaderId, leaderId);
        queryWrapper.orderByAsc(GbGoodsPackageInfo::getPackId);
        List<GbGoodsPackageInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public void updateMiniLeaderGoodsPackageList(Long leaderId, List<GbGoodsPackageInfo> packageList){


        Long goodsId = packageList.get(0).getGoodsId();
        List<GbGoodsPackageInfo> lists = this.getMiniGoodsPackageList(leaderId, goodsId);
        List<Long> ids = new ArrayList<>();
        for(GbGoodsPackageInfo item : packageList){
            if(item.getPackId() > 0){
                this.editMiniLeaderGoodsPackageList(leaderId, item);
                ids.add(item.getPackId());
            }else{
                this.addMiniLeaderGoodsPackageList(leaderId, item);
            }
        }
        for(GbGoodsPackageInfo item : lists){

            if(ids.contains(item.getPackId()) == false){
                this.removeMiniLeaderGoodsPackageList(leaderId, item.getPackId());
            }
        }
    }


    public Long addMiniLeaderGoodsPackageList(Long leaderId, GbGoodsPackageInfo info){

        GbGoodsPackageInfo data = new GbGoodsPackageInfo();
        data.setLeaderId(leaderId);
        data.setGoodsId(info.getGoodsId());
        data.setPackName(info.getPackName());
        data.setSalesPrice(info.getSalesPrice());
        data.setMarketPrice(info.getMarketPrice());
        data.setPackNum(info.getPackNum());
        data.setGoodsUnit(info.getGoodsUnit());
        data.setIsClose((byte)0);
        data.setAddTime(TimeUtils.getTimeStamp());
        mapper.insert(data);
        return data.getPackId();
    }


    public Boolean editMiniLeaderGoodsPackageList(Long leaderId, GbGoodsPackageInfo info){

        LambdaUpdateWrapper<GbGoodsPackageInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbGoodsPackageInfo::getPackName, info.getPackName());
        updateWrapper.set(GbGoodsPackageInfo::getSalesPrice, info.getSalesPrice());
        updateWrapper.set(GbGoodsPackageInfo::getMarketPrice, info.getMarketPrice());
        updateWrapper.set(GbGoodsPackageInfo::getPackNum, info.getPackNum());
        updateWrapper.eq(GbGoodsPackageInfo::getPackId, info.getPackId());
        updateWrapper.eq(GbGoodsPackageInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }

 
    public Boolean closeMiniLeaderGoodsPackageList(int leaderId, int packId, int status){
        LambdaUpdateWrapper<GbGoodsPackageInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbGoodsPackageInfo::getIsClose, status);
        updateWrapper.eq(GbGoodsPackageInfo::getPackId, packId);
        updateWrapper.eq(GbGoodsPackageInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean removeMiniLeaderGoodsPackageList(Long leaderId, Long packId){

        LambdaQueryWrapper<GbGoodsPackageInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGoodsPackageInfo::getPackId, packId);
        queryWrapper.eq(GbGoodsPackageInfo::getLeaderId, leaderId);
        int flag = mapper.delete(queryWrapper);
        return flag > 0 ? true : false;
    }



}
