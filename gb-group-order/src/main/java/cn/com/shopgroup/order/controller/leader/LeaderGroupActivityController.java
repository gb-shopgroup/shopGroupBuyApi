package cn.com.shopgroup.order.controller.leader;

import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import cn.com.shopgroup.order.exception.OrderErrorCodeEnum;
import cn.com.shopgroup.order.http.response.LeaderGroupActivityResponse;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 团长端-团购活动查询
 * <p>
 * 提供团长端活动下拉/选择所需的接口；活动主数据在商品域(goods), 本控制器通过 goods 模块的
 * {@link GbGroupActivityInfoService} 跨模块调用，避免在 order 模块重复建表/写 SQL。
 */
@RestController
@RequestMapping("/order/leader/activity")
@Api(tags = "团长端-团购活动")
@Slf4j
public class LeaderGroupActivityController {

    @Resource
    private GbGroupActivityInfoService groupActivityService;

    //团购活动列表 查询团长 id 下的所有团购活动(不过滤状态/时间, 按添加时间倒序)。
    @GetMapping("/list")
    @ApiOperation("查询团长所有活动")
    public JsonResult list() {
        // 1) 解析团长 id: 优先 Query 参数, 否则请求头 lid
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        // 2) 校验团长 id
        if (leaderId == null || leaderId == 0) {
            log.warn("查询团长所有活动-团长 leaderId={}", leaderId);
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        log.info("查询团长所有活动, leaderId:{}", leaderId);

        // 3) 跨模块调用 goods 服务获取全部活动(不过滤状态/时间)
        List<GbGroupActivityInfo> activities = groupActivityService.listAllByLeaderId(leaderId);

        // 4) 转换为前端下拉所需的精简字段, 避免直接返回 entity 泄露内部字段
        List<LeaderGroupActivityResponse> data = new ArrayList<>();
        if (!CollectionUtils.isEmpty(activities)) {
            for (GbGroupActivityInfo activity : activities) {
                LeaderGroupActivityResponse resp = new LeaderGroupActivityResponse();
                resp.setGroupId(activity.getGroupId());
                resp.setGroupName(activity.getGroupName());
                resp.setStartTime(activity.getStartTime());
                resp.setEndTime(activity.getEndTime());
                resp.setOrderTotal(activity.getOrderTotal());
                resp.setIsClose(activity.getIsClose());
                data.add(resp);
            }
        }
        return JsonResult.success(data);
    }
}