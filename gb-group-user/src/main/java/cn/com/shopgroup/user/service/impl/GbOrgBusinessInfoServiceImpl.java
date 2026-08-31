package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.user.mapper.GbOrgBusinessInfoMapper;
import cn.com.shopgroup.user.model.GbOrgBusinessInfo;
import cn.com.shopgroup.user.service.GbOrgBusinessInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbOrgBusinessInfoServiceImpl implements GbOrgBusinessInfoService {

    @Resource
    private GbOrgBusinessInfoMapper mapper;


    public Long addAdminBusinessInfo(Long leaderId, String busName, String code){

        GbOrgBusinessInfo info = new GbOrgBusinessInfo();

        info.setLeaderId(leaderId);

        info.setBusType((byte)3);

        info.setBusName(busName);

        info.setLegalName("");

        info.setCidNo("");

        info.setCidFront("");

        info.setCidBack("");

        info.setLicenseNo("");

        info.setLicenseFront("");

        info.setLicenseBack("");

        info.setBusBalance(0D);

        info.setTaxLimit(0);

        info.setIsClose((byte)0);

        info.setIsCheck((byte)1);

        info.setCheckRemark("");

        info.setCheckCustId(code);

        info.setAddTime(TimeUtils.getTimeStamp());

        mapper.insert(info);

        return info.getBusId();
    }


    public GbOrgBusinessInfo getAdminBusinessInfoByCode(String code){

        LambdaQueryWrapper<GbOrgBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GbOrgBusinessInfo::getBusId);
        queryWrapper.eq(GbOrgBusinessInfo::getCheckCustId, code);
        queryWrapper.orderByDesc(GbOrgBusinessInfo::getBusId);
        queryWrapper.last("limit 0,1");
        return mapper.selectOne(queryWrapper);
    }


    public List<GbOrgBusinessInfo> getAdminBusinessList(Long leaderId, int page, int pageSize){

        LambdaQueryWrapper<GbOrgBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        if(leaderId > 0) queryWrapper.eq(GbOrgBusinessInfo::getLeaderId, leaderId);
        queryWrapper.orderByDesc(GbOrgBusinessInfo::getBusId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbOrgBusinessInfo> results = mapper.selectList(queryWrapper);
        return results == null ? new ArrayList<>() : results;
    }


    public Long getAdminBusinessCount(){

        LambdaQueryWrapper<GbOrgBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        return mapper.selectCount(queryWrapper);
    }


    public GbOrgBusinessInfo getBusinessInfo(Long busId){

        return mapper.selectById(busId);
    }


    public List<GbOrgBusinessInfo> getMiniBusinessList(Long leaderId){

        LambdaQueryWrapper<GbOrgBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgBusinessInfo::getIsCheck, 0);
        queryWrapper.eq(GbOrgBusinessInfo::getIsClose, 0);
        queryWrapper.eq(GbOrgBusinessInfo::getLeaderId, leaderId);
        queryWrapper.orderByDesc(GbOrgBusinessInfo::getBusId);
        queryWrapper.last("limit 0, 100");
        List<GbOrgBusinessInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public List<GbOrgBusinessInfo> getMiniLeaderBusinessList(Long leaderId){

        LambdaQueryWrapper<GbOrgBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgBusinessInfo::getLeaderId, leaderId);
        queryWrapper.orderByDesc(GbOrgBusinessInfo::getBusId);
        queryWrapper.last("limit 0, 100");
        List<GbOrgBusinessInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Boolean isMiniLeaderAccountExist(String account){

        LambdaQueryWrapper<GbOrgBusinessInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GbOrgBusinessInfo::getBusId);
        queryWrapper.eq(GbOrgBusinessInfo::getCidNo, account);
        queryWrapper.orderByDesc(GbOrgBusinessInfo::getBusId);
        queryWrapper.last("limit 0, 1");
        GbOrgBusinessInfo info = mapper.selectOne(queryWrapper);
        return info == null ? false : true;
    }


    public Long addMiniLeaderBusiness(Long leaderId, GbOrgBusinessInfo info){

        GbOrgBusinessInfo data = new GbOrgBusinessInfo();

        data.setLeaderId(leaderId);

        data.setBusName(info.getBusName());

        data.setLegalName(info.getLegalName());

        byte busType = info.getBusType();
        data.setBusType(busType);
        if(busType == 4){

            data.setCidNo(info.getCidNo());

            data.setCidFront(info.getCidFront());

            data.setCidBack(info.getCidBack());
        }else{

            data.setLicenseNo(info.getLicenseNo());

            data.setLicenseFront(info.getLicenseFront());

            data.setLicenseBack(info.getLicenseBack());
        }

        data.setBusBalance(0D);

        data.setTaxLimit(info.getTaxLimit());

        data.setIsClose((byte)0);

        data.setAddTime(TimeUtils.getTimeStamp());


        mapper.insert(data);
        return data.getBusId();
    }


    public Boolean editMiniLeaderBusiness(Long leaderId, GbOrgBusinessInfo data){

        LambdaUpdateWrapper<GbOrgBusinessInfo> updateWrapper = Wrappers.lambdaUpdate();
 
        updateWrapper.set(GbOrgBusinessInfo::getBusName, data.getBusName());

        updateWrapper.set(GbOrgBusinessInfo::getLegalName, data.getLegalName());

        byte busType = (byte)4;
        updateWrapper.set(GbOrgBusinessInfo::getBusType, data.getBusType());
        if(busType == 4){

            updateWrapper.set(GbOrgBusinessInfo::getCidNo, data.getCidNo());

            updateWrapper.set(GbOrgBusinessInfo::getCidFront, data.getCidFront());

            updateWrapper.set(GbOrgBusinessInfo::getCidBack, data.getCidBack());


            updateWrapper.set(GbOrgBusinessInfo::getLicenseNo, "");

            updateWrapper.set(GbOrgBusinessInfo::getLicenseFront, "");
 
            updateWrapper.set(GbOrgBusinessInfo::getLicenseBack, "");

        }else{

            updateWrapper.set(GbOrgBusinessInfo::getLicenseNo, data.getLicenseNo());

            updateWrapper.set(GbOrgBusinessInfo::getLicenseFront, data.getLicenseFront());
  
            updateWrapper.set(GbOrgBusinessInfo::getLicenseBack, data.getLicenseBack());

     
            updateWrapper.set(GbOrgBusinessInfo::getCidNo, "");

            updateWrapper.set(GbOrgBusinessInfo::getCidFront, "");

            updateWrapper.set(GbOrgBusinessInfo::getCidBack, "");
        }
        updateWrapper.eq(GbOrgBusinessInfo::getBusId, data.getBusId());
        updateWrapper.eq(GbOrgBusinessInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean closeMiniLeaderBusiness(Long leaderId, Long busId, Integer status){

        LambdaUpdateWrapper<GbOrgBusinessInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbOrgBusinessInfo::getIsClose, status);
        updateWrapper.eq(GbOrgBusinessInfo::getBusId, busId);
        updateWrapper.eq(GbOrgBusinessInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean increaseMiniLeaderBusinessBalance(Long leaderId, Long busId, Integer fee){

        LambdaUpdateWrapper<GbOrgBusinessInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.setSql("bus_balance = bus_balance + {0}", Math.abs(fee));
        updateWrapper.eq(GbOrgBusinessInfo::getBusId, busId);
        updateWrapper.eq(GbOrgBusinessInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public Boolean reduceMiniLeaderBusinessBalance(Long leaderId, Long busId, Integer fee){

        LambdaUpdateWrapper<GbOrgBusinessInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.setSql("bus_balance = bus_balance - {0}", Math.abs(fee));
        updateWrapper.eq(GbOrgBusinessInfo::getBusId, busId);
        updateWrapper.eq(GbOrgBusinessInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


}
