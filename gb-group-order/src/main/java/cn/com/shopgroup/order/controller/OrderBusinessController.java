package cn.com.shopgroup.order.controller;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.order.http.response.OrderBusinessInfoResponse;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.order.service.GbOrderBusinessInfoService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

//账户
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderBusinessController {

    @Resource
    private GbOrderBusinessInfoService service;

    // 分页查询订单列表
    @GetMapping("/orderbusiness/list")
    public JsonResult orderList(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {
        log.info("【订单账户管理-分页查询订单列表:/order/orderbusiness/list】params->page:{},pageSize:{}", page, pageSize);

        // 请求参数矫正
        if (page == 0) page = 1;
        if (pageSize == 0) pageSize = 10;
        if (pageSize > 20) pageSize = 20;

        // 查询数据
        List<GbOrderBusinessInfo> result = service.getAdminOrderBusinessList(page, pageSize);
        List<OrderBusinessInfoResponse> data = OrderBusinessInfoResponse.getResponseList(result);
        log.info("【订单账户管理-分页查询订单列表返回】size:{},data:{}", data == null ? 0 : data.size(), JSON.toJSONString(data));
        return JsonResult.success(data);
    }

    // 查询订单总数
    @GetMapping("/orderbusiness/count")
    public JsonResult orderCount() {
        log.info("【订单账户管理-查询订单总数:/order/orderbusiness/count】");
        long total = service.getAdminOrderBusinessCount();
        log.info("【订单账户管理-查询订单总数返回】total:{}", total);
        return JsonResult.success(total);
    }


}
