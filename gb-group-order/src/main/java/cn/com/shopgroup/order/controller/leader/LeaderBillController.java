package cn.com.shopgroup.order.controller.leader;

import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.order.exception.OrderErrorCodeEnum;
import cn.com.shopgroup.order.http.request.LeaderBillListRequest;
import cn.com.shopgroup.order.http.response.LeaderBillListResponse;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 团长端-对账单
 */
@RestController
@Slf4j
@RequestMapping("/order/leader/bill")
public class LeaderBillController {

    @Resource
    private GbOrderInfoService orderInfoService;

    // 团长端-对账单: 按选择的时间范围查询(未输入时间不查询, 直接返回空账单);
    // 统计维度type: 1=按商品(明细返回商品名称), 2=按订单(明细返回订单号);
    // 顶部统计=有效订单数(已支付且未取消, 按下单时间归集)/订单总金额/退款总金额,
    // 金额单位:元; 按订单维度订单金额=实付金额, 按商品维度订单金额=Σ商品单价×购买数量、退款金额=Σ商品单价×退款数量
    @PostMapping("/list")
    public JsonResult billList(@RequestBody LeaderBillListRequest request) {
        log.info("团长端-对账单查询，参数request:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == null || leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 查询对账单
        LeaderBillListResponse data = orderInfoService.getLeaderBillList(leaderId, request);
        log.info("团长端-对账单查询结果:{}", JSON.toJSONString(data));
        return JsonResult.success(data);
    }
}
