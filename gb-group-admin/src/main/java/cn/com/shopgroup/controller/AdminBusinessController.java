package cn.com.shopgroup.controller;

import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.exception.AdminErrorCodeEnum;
import cn.com.shopgroup.http.request.LeaderBusinessRequest;
import cn.com.shopgroup.order.model.GbLeaderMerchantMonthlyAmount;
import cn.com.shopgroup.order.service.GbLeaderMerchantMonthlyAmountService;
import cn.com.shopgroup.user.model.GbOrgBusinessInfo;
import cn.com.shopgroup.user.service.GbOrgBusinessInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class AdminBusinessController {

    @Resource
    private GbOrgBusinessInfoService service;

    @Resource
    private GbLeaderMerchantMonthlyAmountService merchantMonthlyAmountService;


    // 分页查询团长收款账户列表(含累计额度)
    @GetMapping("/admin/business/list")
    public JsonResult businessList(@RequestParam("id") Long leaderId, @RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {

        if (page == 0) page = 1;
        if (pageSize == 0) pageSize = 10;
        if (pageSize > 20) pageSize = 20;
        List<GbOrgBusinessInfo> data = service.getAdminBusinessList(leaderId, page, pageSize);

        // 从商户月收入表获取当月累计收款额度(替代原 Redis 月度 ZSet)
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        for (GbOrgBusinessInfo item : data) {
            GbLeaderMerchantMonthlyAmount monthlyAmount =
                    merchantMonthlyAmountService.getByMerchantAndYearMonth(item.getLeaderId(), item.getCheckCustId(), yearMonth);
            if (monthlyAmount != null) {
                BigDecimal total = monthlyAmount.getTotalAmount();
                item.setBusBalance(total == null ? 0D : total.doubleValue());
            }
        }

        // 返回
        return JsonResult.success(data);
    }

    // 查询收款账户总数
    @GetMapping("/admin/business/count")
    public JsonResult businessCount() {

        long total = service.getAdminBusinessCount();
        return JsonResult.success(total);
    }

    // 查询收款账户详情
    @GetMapping("/admin/business/info")
    public JsonResult businessInfo(@RequestParam("id") Long id) {

        GbOrgBusinessInfo info = service.getBusinessInfo(id);
        return JsonResult.success(info);
    }

    // 添加团长收款账户(商户编号需唯一)
    @PostMapping("/admin/business/add")
    public JsonResult businessAdd(@RequestBody LeaderBusinessRequest request) {

        // 商户编号已经存在
        GbOrgBusinessInfo businessInfo = service.getAdminBusinessInfoByCode(request.getShopCode());
        if (businessInfo != null) throw new BusinessException(AdminErrorCodeEnum.MERCHANT_NO_EXISTED);

        // 添加收款商户
        service.addAdminBusinessInfo(request.getLeaderId(), request.getShopName(), request.getShopCode());
        return JsonResult.success();
    }


}
