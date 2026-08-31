package cn.com.shopgroup.user.controller.group;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.user.http.response.PointResponse;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.service.GbOrgPointInfoService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
@Api(value = "团长端-自提点管理")
public class GroupPointController {

    @Resource
    private GbOrgPointInfoService orgPointInfoService;
    @Resource
    private RedisHelper redisHelper;

    // 自提点列表
    @GetMapping("/group/point")
    public JsonResult point(@RequestParam("id") Long leaderId) {
        log.info("[get] /group/point param:leaderId:{}", leaderId);
        // 先读缓存再读数据库
        String key = RedisConstant.RedisPointListKey + leaderId;
        if (redisHelper.hasKey(key) == false) {

            // 查询数据库
            List<GbOrgPointInfo> result = orgPointInfoService.getMiniPointList(leaderId);
            List<PointResponse> data = PointResponse.getPointResponseList(result);

            // 缓存到Redis
            redisHelper.setCacheObject(key, data, RedisConstant.RedisPointListExpired, TimeUnit.SECONDS);
            return JsonResult.success(data);
        }

        // 读取数据库
        List<PointResponse> data = redisHelper.getCacheObject(key);
        return JsonResult.success(data);
    }

}
