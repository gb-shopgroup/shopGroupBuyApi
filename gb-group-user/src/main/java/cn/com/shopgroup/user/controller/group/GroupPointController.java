package cn.com.shopgroup.user.controller.group;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.user.http.response.PointResponse;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.service.GbOrgPointInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class GroupPointController {

    @Resource
    private GbOrgPointInfoService orgPointInfoService;

    // 自提点列表
    @GetMapping("/group/point")
    public JsonResult point(@RequestParam("id") Long leaderId) {
        log.info("[get] /group/point param:leaderId:{}", leaderId);
        List<GbOrgPointInfo> result = orgPointInfoService.getMiniPointList(leaderId);
        if (CollectionUtils.isEmpty(result)) {
            return JsonResult.success();
        }
        List<PointResponse> data = PointResponse.getPointResponseList(result);
        return JsonResult.success(data);
    }
}
