package cn.com.shopgroup.controller;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class AdminGroupController {

    @Resource
    private GbGroupActivityInfoService service;

    // 分页查询团购活动列表
    @GetMapping("/admin/group/list")
    public JsonResult groupList(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {

        if (page == 0) page = 1;
        if (pageSize == 0) pageSize = 10;
        if (pageSize > 100) pageSize = 100;
        List<GbGroupActivityInfo> result = service.getAdminGroupList(page, pageSize);
        return JsonResult.success(result);
    }

    // 查询团购活动总数
    @GetMapping("/admin/group/count")
    public JsonResult groupCount() {

        long total = service.getAdminGroupCount();
        return JsonResult.success(total);
    }

    // 查询团购活动详情
    @GetMapping("/admin/group/info")
    public JsonResult groupInfo(@RequestParam("id") Long id) {

        GbGroupActivityInfo info = service.getGroupInfo(id);
        return JsonResult.success(info);
    }


}
