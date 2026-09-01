package cn.com.shopgroup.user.controller.leader;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.user.http.request.BusinessRequest;
import cn.com.shopgroup.user.http.request.OCBusinessRequest;
import cn.com.shopgroup.user.http.response.BusinessResponse;
import cn.com.shopgroup.user.model.GbOrgBusinessInfo;
import cn.com.shopgroup.user.service.GbOrgBusinessInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user")
public class LeaderBusinessController {

    @Resource
    private GbOrgBusinessInfoService service;


    /**
     * 查询当前团长收款账户列表（含Redis累计额度）
     */
    @GetMapping("/leader/business/list")
    public JsonResult businessList() {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 查询收款账户列表
        List<GbOrgBusinessInfo> result = service.getMiniLeaderBusinessList(leaderId);
        log.info("/leader/business/list leaderId:{},ret:{}", leaderId, JSON.toJSONString(result));
        if (CollectionUtils.isEmpty(result)) {
            return JsonResult.success();
        }
        List<BusinessResponse> data = BusinessResponse.getBusinessResponseList(result);
        return JsonResult.success(data);
    }

    /**
     * 添加团长收款账户（校验证件号码唯一，返回新增账户ID）
     */
    @PostMapping("/leader/business/add")
    public JsonResult addBusiness(@Validated @RequestBody BusinessRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 证件号码不能重复
        boolean isExist = service.isMiniLeaderAccountExist(request.getNo());
        if (isExist) return JsonResult.fail("证件号码已经存在");

        // 添加账户信息
        GbOrgBusinessInfo data = new GbOrgBusinessInfo();
        data.setBusType(request.getType());
        data.setBusName(request.getName());
        data.setLegalName(request.getLegal());
        if (request.getType() == 4) {
            data.setCidNo(request.getNo());
            data.setCidFront(request.getFront());
            data.setCidBack(request.getBack());
        } else {
            data.setLicenseNo(request.getNo());
            data.setLicenseFront(request.getFront());
            data.setLicenseBack(request.getBack());
        }
        data.setTaxLimit(request.getTax());
        Long busId = service.addMiniLeaderBusiness(leaderId, data);
        return JsonResult.success("添加成功", busId);
    }

    /**
     * 修改收款账户（已审核通过的账户不允许修改）
     */
    @PostMapping("/leader/business/edit")
    public JsonResult editBusiness(@Validated @RequestBody BusinessRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 已经审核通过后不能修改
        GbOrgBusinessInfo businessInfo = service.getBusinessInfo(request.getId());
        if (ObjectUtils.isEmpty(businessInfo)) {
            return JsonResult.fail("未查到到相关信息");
        }
        if (businessInfo.getIsCheck() == 1) {
            return JsonResult.fail("已审核, 不允许修改");
        }
        // 添加账户信息
        GbOrgBusinessInfo data = new GbOrgBusinessInfo();
        data.setBusId(request.getId());
        data.setBusType(request.getType());
        data.setBusName(request.getName());
        data.setLegalName(request.getLegal());
        if (request.getType() == 4) {
            data.setCidNo(request.getNo());
            data.setCidFront(request.getFront());
            data.setCidBack(request.getBack());
        } else {
            data.setLicenseNo(request.getNo());
            data.setLicenseFront(request.getFront());
            data.setLicenseBack(request.getBack());
        }
        data.setTaxLimit(request.getTax());
        boolean flag = service.editMiniLeaderBusiness(leaderId, data);
        if (flag) {
            return JsonResult.success();
        } else {
            return JsonResult.fail();
        }
    }

    /**
     * 关闭/启用收款账户（禁用或启用商户收款）
     */
    @PostMapping("/leader/business/close")
    public JsonResult closeBusiness(@Validated @RequestBody OCBusinessRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        service.closeMiniLeaderBusiness(leaderId, request.getBusId(), request.getStatus());
        return JsonResult.success();
    }

}
