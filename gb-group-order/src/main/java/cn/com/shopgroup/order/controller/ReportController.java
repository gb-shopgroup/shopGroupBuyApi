package cn.com.shopgroup.order.controller;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.order.http.response.ReportResponse;
import cn.com.shopgroup.order.model.GbReportBusinessInfo;
import cn.com.shopgroup.order.service.GbReportBusinessInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/order")
public class ReportController {

    @Resource
    private GbReportBusinessInfoService service;

    // 分账汇总列表
    @GetMapping("/leader/report/business/list")
    public JsonResult reportBusinessList(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 请求参数矫正
        if (page == 0) page = 1;
        if (pageSize == 0) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        // 查询列表
        List<GbReportBusinessInfo> lists = service.getMiniLeaderReportBusinessList(leaderId, page, pageSize);
        List<ReportResponse> data = ReportResponse.getReportResponseList(lists);
        return JsonResult.success("查询成功", data);
    }

    // 分账汇总数量
    @GetMapping("/leader/report/business/count")
    public JsonResult reportBusinessCount() {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 查询总数量
        long total = service.getMiniLeaderReportBusinessCount(leaderId);
        return JsonResult.success("查询成功", total);
    }

}
