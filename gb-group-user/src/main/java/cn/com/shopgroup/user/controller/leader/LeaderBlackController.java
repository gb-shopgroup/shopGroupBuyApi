package cn.com.shopgroup.user.controller.leader;

import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.user.exception.UserErrorCodeEnum;
import cn.com.shopgroup.user.http.response.LeaderMemberResponse;
import cn.com.shopgroup.user.model.GbMemberBlackList;
import cn.com.shopgroup.user.model.GbMemberInfo;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import cn.com.shopgroup.user.service.GbMemberBlackListService;
import cn.com.shopgroup.user.service.GbMemberInfoService;
import cn.com.shopgroup.user.service.GbOrgStaffInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class LeaderBlackController {

    @Resource
    private GbMemberInfoService memberInfoService;

    @Resource
    private GbMemberBlackListService blackService;

    @Resource
    private GbOrgStaffInfoService staffService;

    // 根据手机号查询用户
    @GetMapping("/leader/member/mobile")
    public JsonResult getMemberMobile(@RequestParam("mobile") String mobile) {
        log.info("/user/leader/member/mobile req mobile:{}", mobile);
        GbMemberInfo data = memberInfoService.getMemberInfoByMobile(mobile);
        log.info("data from db info:{}", JSON.toJSONString(data));
        if (ObjectUtils.isEmpty(data)) {
            throw new BusinessException(UserErrorCodeEnum.USER_NOT_EXIST);
        }
        LeaderMemberResponse response = new LeaderMemberResponse(data);
        return JsonResult.success(response);
    }

    // 加入黑名单
    @PostMapping("/leader/add/black")
    public JsonResult addMemberBlack(@RequestParam("memberId") Long memberId) {
        log.info("/user/leader//add/black req memberId:{}", memberId);
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 构建黑名单数据
        GbMemberBlackList data = new GbMemberBlackList();
        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        if (staffId != 0) {
            // 查询店员
            GbOrgStaffInfo staffInfo = staffService.getStaffInfo(staffId);
            if (ObjectUtils.isEmpty(staffInfo)) {
                throw new BusinessException(UserErrorCodeEnum.STAFF_QUERY_ERROR);
            }
            data.setStaffId(staffInfo.getStaffId());
            data.setStaffName(staffInfo.getStaffName());
        }
        // 是否已经添加黑名单
        boolean isExists = blackService.getMemberBlackById(leaderId, memberId);
        if (isExists) {
            throw new BusinessException(UserErrorCodeEnum.BLACKLIST_EXISTED);
        }
        // 查询用户信息
        GbMemberInfo info = memberInfoService.getMemberInfo(memberId);
        if (ObjectUtils.isEmpty(info)) {
            throw new BusinessException(UserErrorCodeEnum.USER_NOT_EXIST);
        }
        data.setMemberId(info.getMemberId());
        data.setMobile(info.getMobile());
        data.setNickName(info.getNickname());
        data.setAvatar(info.getAvatar());
        // 写入数据库
        boolean isSuccess = blackService.addMemberBlack(leaderId, data);
        if (isSuccess) {
            return JsonResult.success();
        } else {
            throw new BusinessException(UserErrorCodeEnum.UPDATE_FAILED);
        }
    }

    // 根据手机号查询黑名单用户
    @GetMapping("/leader/black/mobile")
    public JsonResult queryMemberBlack(@RequestParam("mobile") String mobile) {
        log.info("根据手机号查询黑名单用户 req mobile:{}", mobile);
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 查询是否已经黑名单
        GbMemberBlackList blackInfo = blackService.getMemberBlackByMobile(leaderId, mobile);
        if (ObjectUtils.isEmpty(blackInfo)) {
            throw new BusinessException(UserErrorCodeEnum.BLACKLIST_NOT_EXIST);
        }
        // 返回数据
        LeaderMemberResponse response = new LeaderMemberResponse(blackInfo);
        return JsonResult.success(response);
    }

    // 解除黑名单
    @PostMapping("/leader/member/black/remove")
    public JsonResult removeMemberBlack(@RequestParam("memberId") Long memberId) {
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 解除黑名单
        boolean isSuccess = blackService.removeMemberBlack(leaderId, memberId);
        if (isSuccess) {
            return JsonResult.success();
        } else {
            throw new BusinessException(UserErrorCodeEnum.UPDATE_FAILED);
        }
    }

    // 黑名单列表
    @GetMapping("/leader/black/list")
    public JsonResult getMemberBlackList(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }

        // 请求参数矫正
        if (page == 0) page = 1;
        if (pageSize == 0) pageSize = 10;
        if (pageSize > 20) pageSize = 20;

        // 分页查询
        List<GbMemberBlackList> results = blackService.getMemberBlackList(leaderId, page, pageSize);
        List<LeaderMemberResponse> lists = LeaderMemberResponse.getMemberResponseList(results);
        return JsonResult.success(lists);
    }

    // 黑名单总数
    @GetMapping("/leader/black/count")
    public JsonResult getMemberBlackCount() {
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 查询数量
        long total = blackService.getMemberBlackCount(leaderId);
        return JsonResult.success(total);
    }


}
