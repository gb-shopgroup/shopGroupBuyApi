package cn.com.shopgroup.controller;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.user.model.GbMemberInfo;
import cn.com.shopgroup.user.service.GbMemberInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class AdminMemberController {

    @Resource
    private GbMemberInfoService service;

    // 分页查询会员列表
    @GetMapping("/admin/member/list")
    public JsonResult memberList(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {

        List<GbMemberInfo> result = service.getAdminMemberList(page, pageSize);
        return JsonResult.success(result);
    }

    // 查询会员总数
    @GetMapping("/admin/member/count")
    public JsonResult memberCount() {

        long total = service.getAdminMemberCount();
        return JsonResult.success(total);
    }


}
