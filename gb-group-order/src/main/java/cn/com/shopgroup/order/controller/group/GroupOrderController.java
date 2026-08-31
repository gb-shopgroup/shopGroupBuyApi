package cn.com.shopgroup.order.controller.group;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TokenUtils;
import cn.com.shopgroup.order.http.request.OrderRequest;
import cn.com.shopgroup.order.service.OrderService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/order/group")
@Slf4j
public class GroupOrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private RedisHelper redisHelper;


    // 用户下单
    @PostMapping("/add")
    public JsonResult order(@RequestBody OrderRequest request) {
        log.info("【订单系统-下单接口:/order/add】request:{}", JSON.toJSON(request));
        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            return JsonResult.fail("token不存在");
        }
        String userId = TokenUtils.parseToken(token);
        if (StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            return JsonResult.fail("用户不存在");
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            JsonResult.fail("用户不存在");
        }
        if (memberId == 0) {
            JsonResult.fail("用户不存在");
        }

        // 防刷
        String key = RedisConstant.RedisOrderAddKey + memberId + request.getGroupId();
        if (redisHelper.hasKey(key)) {
            return JsonResult.fail("系统繁忙, 请稍后提交订单");
        } else {
            redisHelper.setCacheObject(key, memberId, RedisConstant.RedisOrderAddExpired, TimeUnit.SECONDS);
        }

        // 下单
        Map<String, String> res = orderService.addOrder(memberId, request);
        int success = Integer.parseInt(res.get("success"));
        String msg = res.get("msg"); // 订单id
        if (success == 1) {
            return JsonResult.success("下单成功", msg);
        } else {
            return JsonResult.fail(msg);
        }
    }

}
