package cn.com.shopgroup.order.controller.group;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/order")
public class WxOrderController {

    @Resource
    private GbOrderInfoService orderInfoService;

    @Resource
    private WxMiniAccessTokenHelper helper;
    //查询微信订单发货状态（查询订单状态枚举：(1) 待发货；(2) 已发货；(3) 确认收货；(4) 交易完成；(5) 已退款；(6) 资金待结算）
    @GetMapping("/group/wx/order")
    public JsonResult wxOrder(@RequestParam("orderNo") String orderNo) {
        log.info("【查询订单发货状态接口：/order/group/wx/order】 orderNo:{}", orderNo);
        // 查询订单详情
        GbOrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);
        log.info("【接口：/group/order/wx/order】 查询结果-orderInfo:{}", JSON.toJSON(orderInfo));
        if (ObjectUtils.isEmpty(orderInfo)) {
            return JsonResult.fail("订单不存在");
        }

        // 订单必须支付才行
        Integer orderStatus = orderInfo.getStatus();
        if (orderStatus.intValue() == 0) {
            return JsonResult.fail("订单未支付");
        }
        // 查询微信流水号
        String transactionId = orderInfo.getPayNo();
        if (StringUtils.isEmpty(transactionId)) {
            return JsonResult.fail("订单未支付");
        }
        // 获取 accessToken
        String accessToken = helper.getAccessToken(false);
        // 查询订单状态枚举：(1) 待发货；(2) 已发货；(3) 确认收货；(4) 交易完成；(5) 已退款；(6) 资金待结算。
        Integer status = WxMiniProgramHelper.getWxOrder(accessToken, transactionId);
        if (status.intValue() > 0) {
            // 返回成功
            return JsonResult.success("查询成功", status);
        } else {
            if (status == -1) {
                helper.removeAccessToken();
                return JsonResult.fail("查询失败，请稍后再试！");
            }
        }
        return JsonResult.success("查询成功", status);
    }

}
