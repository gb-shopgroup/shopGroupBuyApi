package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.mapper.GbGoodsSkuInfoMapper;
import cn.com.shopgroup.goods.model.GbGoodsSkuInfo;
import cn.com.shopgroup.goods.service.GbGoodsSkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.executor.BatchResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbGoodsSkuInfoServiceImpl implements GbGoodsSkuInfoService {

    @Resource
    private GbGoodsSkuInfoMapper mapper;

    public GbGoodsSkuInfo getGoodsSkuInfo(Long skuId) {
        return mapper.selectById(skuId);
    }


    public List<GbGoodsSkuInfo> getMiniGoodsSkuList(Long goodsId) {
        LambdaQueryWrapper<GbGoodsSkuInfo> queryWrapper = Wrappers.lambdaQuery();
        // 查询全部字段, 保证前端能拿到 skuNames/skuImages/marketPrice 等完整SKU信息(后台自动生成SKU后展示依赖)
        queryWrapper.eq(GbGoodsSkuInfo::getIsClose, 0);
        queryWrapper.eq(GbGoodsSkuInfo::getGoodsId, goodsId);
        List<GbGoodsSkuInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public GbGoodsSkuInfo getMiniGoodsSkuStockPrice(Long skuId) {

        LambdaQueryWrapper<GbGoodsSkuInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GbGoodsSkuInfo::getGoodsNum, GbGoodsSkuInfo::getSalesPrice);
        queryWrapper.eq(GbGoodsSkuInfo::getSkuId, skuId);
        return mapper.selectOne(queryWrapper);
    }


    public List<GbGoodsSkuInfo> getMiniLeaderGoodsSkuList(Long goodsId) {
        LambdaQueryWrapper<GbGoodsSkuInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGoodsSkuInfo::getGoodsId, goodsId);
        queryWrapper.orderByAsc(GbGoodsSkuInfo::getSkuId);
        queryWrapper.last("limit 0, 100");
        List<GbGoodsSkuInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Integer saveMiniLeaderGoodsSku(Long leaderId, Long goodsId, List<GbGoodsSkuInfo> lists) {
        LambdaQueryWrapper<GbGoodsSkuInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGoodsSkuInfo::getGoodsId, goodsId);
        queryWrapper.eq(GbGoodsSkuInfo::getLeaderId, leaderId);
        mapper.delete(queryWrapper);
        int nowTime = TimeUtils.getTimeStamp();
        List<GbGoodsSkuInfo> dataList = new ArrayList<>();
        for (GbGoodsSkuInfo item : lists) {
            GbGoodsSkuInfo data = new GbGoodsSkuInfo();
            data.setGoodsId(item.getGoodsId());
            data.setLeaderId(leaderId);
            data.setSkuIds(item.getSkuIds());
            data.setSkuNames(item.getSkuNames());
            data.setCostPrice(item.getCostPrice());
            data.setSalesPrice(item.getSalesPrice());
            data.setMarketPrice(item.getMarketPrice());
            data.setGoodsNum(item.getGoodsNum());
            data.setPackNum(item.getPackNum());
            data.setGoodsUnit(item.getGoodsUnit());
            data.setGoodsImg(item.getGoodsImg());
            data.setIsClose((byte) 0);
            data.setAddTime(nowTime);
            dataList.add(data);
        }
        List<BatchResult> res = mapper.insert(dataList);
        if (!CollectionUtils.isEmpty(res)) {
            return res.size();
        } else {
            return 0;
        }

    }


    public Boolean reduceGoodsStock(Long skuId, Integer goodsNum) {

        int num = Math.abs(goodsNum);
        LambdaUpdateWrapper<GbGoodsSkuInfo> updateWrapper = Wrappers.lambdaUpdate();
        // 原子扣减并防超扣: 仅当库存 >= 扣减数时才更新, 否则返回false表示库存不足
        updateWrapper.setSql("goods_num = goods_num - {0}", num);
        updateWrapper.ge(GbGoodsSkuInfo::getGoodsNum, num);
        updateWrapper.eq(GbGoodsSkuInfo::getSkuId, skuId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean increaseGoodsStock(Long skuId, Integer goodsNum) {
        LambdaUpdateWrapper<GbGoodsSkuInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.setSql("goods_num = goods_num + {0}", Math.abs(goodsNum));
        updateWrapper.eq(GbGoodsSkuInfo::getSkuId, skuId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }

}
