package cn.com.shopgroup.user.controller;

import cn.com.shopgroup.common.merchant.MerchantInfo;
import cn.com.shopgroup.common.merchant.MerchantService;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.user.http.request.LeaderBusinessRequest;
import cn.com.shopgroup.user.model.GbOrgBusinessInfo;
import cn.com.shopgroup.user.service.GbOrgBusinessInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user")
public class BusinessController {

    @Resource
    private GbOrgBusinessInfoService service;

    @Resource
    private MerchantService merchantService;


    // 分页查询团长收款账户列表(含Redis累计额度)
    @GetMapping("business/list")
    public JsonResult businessList(@RequestParam("id") Long leaderId, @RequestParam("page") int page, @RequestParam("pageSize") int pageSize){

        if(page == 0) page = 1;
        if(pageSize == 0) pageSize = 10;
        if(pageSize > 100) pageSize = 100;
        List<GbOrgBusinessInfo> data = service.getAdminBusinessList(leaderId, page, pageSize);

        // 从Redis中获取累计额度
        for(GbOrgBusinessInfo item : data){
            MerchantInfo info = merchantService.getLeaderMerchantInfo(item.getLeaderId(), item.getBusId());
            if(info != null) item.setBusBalance(MoneyUtil.centToYuan(info.getMoney()));
        }

        // 返回
        return JsonResult.success(data);
    }

    // 查询收款账户总数
    @GetMapping("/business/count")
    public JsonResult businessCount(){

        Long total = service.getAdminBusinessCount();
        return JsonResult.success(total);
    }

    // 查询收款账户详情
    @GetMapping("/business/info")
    public JsonResult businessInfo(@RequestParam("id") Long id){

        GbOrgBusinessInfo info = service.getBusinessInfo(id);
        return JsonResult.success(info);
    }

    // 添加团长收款商户(校验商户编号唯一)
    @PostMapping("/business/add")
    public JsonResult businessAdd(@RequestBody LeaderBusinessRequest request){

        // 商户编号已经存在
        GbOrgBusinessInfo businessInfo = service.getAdminBusinessInfoByCode(request.getShopCode());
        if(businessInfo != null) return JsonResult.fail("商户编号已经存在");

        // 添加收款商户
        service.addAdminBusinessInfo(request.getLeaderId(), request.getShopName(), request.getShopCode());
        return JsonResult.success();
    }


}
