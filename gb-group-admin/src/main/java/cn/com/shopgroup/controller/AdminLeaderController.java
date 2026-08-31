package cn.com.shopgroup.controller;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.http.request.LeaderRequest;
import cn.com.shopgroup.http.response.OptionResponse;
import cn.com.shopgroup.user.model.GbMemberInfo;
import cn.com.shopgroup.user.model.GbOrgBusinessInfo;
import cn.com.shopgroup.user.model.GbOrgLeaderInfo;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import cn.com.shopgroup.user.service.GbMemberInfoService;
import cn.com.shopgroup.user.service.GbOrgBusinessInfoService;
import cn.com.shopgroup.user.service.GbOrgLeaderInfoService;
import cn.com.shopgroup.user.service.GbOrgShopInfoService;
import cn.com.shopgroup.user.service.GbOrgStaffInfoService;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
public class AdminLeaderController {

    @Resource
    private GbOrgLeaderInfoService service;

    @Resource
    private GbOrgStaffInfoService staffInfoService;

    @Resource
    private GbOrgShopInfoService shopInfoService;

    @Resource
    private GbOrgBusinessInfoService businessInfoService;

    @Resource
    private GbMemberInfoService memberInfoService;


    // 分页查询团长列表(可按手机号筛选)
    @GetMapping("/admin/leader/list")
    public JsonResult leaderList(@RequestParam("mobile") String mobile, @RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {

        List<GbOrgLeaderInfo> result = service.getAdminLeaderList(mobile, page, pageSize);
        return JsonResult.success(result);
    }

    // 查询团长总数
    @GetMapping("/admin/leader/count")
    public JsonResult leaderCount() {

        long total = service.getAdminLeaderCount();
        return JsonResult.success(total);
    }

    // 添加团长(校验手机号/商户编号, 同步创建员工/店铺/收款账户)
    @PostMapping("/admin/leader/add")
    public JsonResult addLeader(@RequestBody @Validated LeaderRequest request) {

        // 根据手机号查询用户信息
        String mobile = request.getMobile();
        GbMemberInfo memberInfo = memberInfoService.getMemberInfoByMobile(mobile);
        if (memberInfo == null) return JsonResult.fail("小程序用户不存在");

        // 手机号是否已经存在
        GbOrgStaffInfo staffInfo = staffInfoService.getAdminLeaderStaffInfoByMobile(request.getMobile());
        if (!ObjectUtils.isEmpty(staffInfo)) {
            return JsonResult.fail("手机号已存在");
        }

        // 商编是否已经存在
        GbOrgBusinessInfo businessInfo = businessInfoService.getAdminBusinessInfoByCode(request.getShopCode());
        if (!ObjectUtils.isEmpty(businessInfo)) {
            return JsonResult.fail("商户编号已存在");
        }

        // 获取用户昵称，头像和openid
        String nickName = memberInfo.getNickname();
        String avatar = memberInfo.getAvatar();
        String openid = memberInfo.getOpenid();

        // 添加团长信息
        Long leaderId = service.addAdminLeaderInfo(request.getName(), mobile, nickName, avatar, openid, request.getCommission());
        if (leaderId.intValue() <= 0) {
            return JsonResult.fail("添加失败");
        }

        // 添加人员信息
        Long staffId = staffInfoService.addAdminLeaderStaffInfo(leaderId, request.getName(), mobile, nickName, avatar, openid);
        if (staffId.intValue() <= 0) {
            return JsonResult.fail("添加失败");
        }

        // 添加店铺信息
        shopInfoService.addAdminLeaderShop(leaderId, request.getShopName());

        // 添加收款账户信息
        businessInfoService.addAdminBusinessInfo(leaderId, request.getShopName(), request.getShopCode());

        // 返回
        return JsonResult.success();
    }

    // 团长下拉选项列表(id/名称)
    @GetMapping("/admin/leader/select")
    public JsonResult leaderSelect() {

        List<OptionResponse> data = new ArrayList<>();
        List<GbOrgLeaderInfo> lists = service.getAdminLeaderSelectList();
        for (GbOrgLeaderInfo temp : lists) {
            data.add(new OptionResponse(temp.getLeaderId().intValue(), temp.getLeaderName()));
        }
        return JsonResult.success(data);
    }


}
