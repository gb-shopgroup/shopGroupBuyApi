package cn.com.shopgroup.order.controller.leader;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.order.http.request.LeaderMemberListRequest;
import cn.com.shopgroup.order.http.response.LeaderMemberDetailResponse;
import cn.com.shopgroup.order.http.response.LeaderMemberListResponse;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 团长端-我的团员
 */
@RestController
@Slf4j
@RequestMapping("/order/leader/myMember")
public class LeaderMemberController {

    @Resource
    private GbOrderInfoService orderInfoService;

    /**
     * 我的团员列表（支持手机号/昵称搜索，分页）
     */
    @PostMapping("/list")
    public JsonResult memberList(@RequestBody LeaderMemberListRequest request) {
        log.info("团长端-我的团员列表，参数request:{}", JSON.toJSONString(request));
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == null || leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize())
                .map(size -> Math.min(size, 100))
                .orElse(10);
        List<LeaderMemberListResponse> data = orderInfoService.getLeaderMemberList(
                leaderId, request.getKeyword(), page, pageSize);
        return JsonResult.success(data);
    }

    /**
     * 团员详情（消费/退款/跟团次数/查看次数/动态）
     */
    @GetMapping("/detail")
    public JsonResult memberDetail(@RequestParam("memberId") Long memberId) {
        log.info("团长端-团员详情，memberId:{}", memberId);
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == null || leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        if (memberId == null || memberId <= 0) {
            return JsonResult.fail("memberId参数错误");
        }
        LeaderMemberDetailResponse data = orderInfoService.getLeaderMemberDetail(leaderId, memberId);
        return JsonResult.success(data);
    }
}
