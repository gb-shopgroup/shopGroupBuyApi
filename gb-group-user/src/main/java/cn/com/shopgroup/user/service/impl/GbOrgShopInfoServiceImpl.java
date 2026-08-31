package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbOrgShopInfoMapper;
import cn.com.shopgroup.user.model.GbOrgShopInfo;
import cn.com.shopgroup.user.service.GbOrgShopInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GbOrgShopInfoServiceImpl implements GbOrgShopInfoService {

    @Resource
    private GbOrgShopInfoMapper mapper;


    public void addAdminLeaderShop(Long leaderId, String shopName) {

        GbOrgShopInfo info = new GbOrgShopInfo();

        info.setLeaderId(leaderId);

        info.setShopName(shopName);

        info.setShopLogo("");

        info.setShopBanner("");

        info.setShopMobile("");

        info.setShopInfo("");

        info.setShopCodeUrl("");

        mapper.insert(info);
    }


    public GbOrgShopInfo getMiniLeaderShop(Long leaderId) {
        return mapper.getMiniLeaderShop(leaderId);
    }


    public Boolean updateMiniLeaderShop(Long leaderId, Long shopId, String name, String mobile, String banner) {
        LambdaUpdateWrapper<GbOrgShopInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrgShopInfo::getId, shopId);
        updateWrapper.set(GbOrgShopInfo::getShopName, name);
        updateWrapper.set(GbOrgShopInfo::getShopMobile, mobile);
        if (banner != null && banner.length() > 0)
            updateWrapper.set(GbOrgShopInfo::getShopBanner, banner);
        updateWrapper.eq(GbOrgShopInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }

    @Override
    public GbOrgShopInfo getInfoByLeaderAndShopId(Long leaderId, Long shopId) {
        LambdaQueryWrapper<GbOrgShopInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgShopInfo::getLeaderId, leaderId);
        queryWrapper.eq(GbOrgShopInfo::getShopInfo, shopId);
        return mapper.selectOne(queryWrapper);
    }

    @Override
    public Boolean updateShopInfo(GbOrgShopInfo shopInfo) {
        int flag = mapper.updateById(shopInfo);
        return flag > 0 ? true : false;
    }

    @Override
    public GbOrgShopInfo getByShopId(Long shopId) {
        return mapper.selectById(shopId);
    }


}
