package cn.com.shopgroup.controller;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.order.service.GbOrderBusinessInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class AdminOrderBusinessController {

    @Resource
    private GbOrderBusinessInfoService orderBusinessInfoService;

    // 分页查询订单列表
    @GetMapping("/admin/orderbusiness/list")
    public JsonResult orderList(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {

        // 请求参数矫正
        if (page == 0) page = 1;
        if (pageSize == 0) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        // 查询数据
        List<GbOrderBusinessInfo> result = orderBusinessInfoService.getAdminOrderBusinessList(page, pageSize);
        return JsonResult.success(result);
    }

    // 查询订单总数
    @GetMapping("/admin/orderbusiness/count")
    public JsonResult orderCount() {

        long total = orderBusinessInfoService.getAdminOrderBusinessCount();
        return JsonResult.success(total);
    }


}
