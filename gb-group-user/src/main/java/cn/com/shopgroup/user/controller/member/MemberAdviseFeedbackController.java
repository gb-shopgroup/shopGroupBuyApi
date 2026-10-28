package cn.com.shopgroup.user.controller.member;

import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TokenUtils;
import cn.com.shopgroup.user.exception.UserErrorCodeEnum;
import cn.com.shopgroup.user.http.request.AdviseFeedbackRequest;
import cn.com.shopgroup.user.http.response.AdviseFeedbackResponse;
import cn.com.shopgroup.user.model.GbMemberAdviseFeedback;
import cn.com.shopgroup.user.service.GbMemberAdviseFeedbackService;
import com.alibaba.fastjson2.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户建议反馈(会员端): 提交/修改/删除/查询反馈
 */
@RestController
@RequestMapping("user/member/advise")
@Api(value = "用户建议反馈", tags = "用户建议反馈")
@Slf4j
public class MemberAdviseFeedbackController {

    @Resource
    private GbMemberAdviseFeedbackService service;

    // 提交建议反馈
    @ApiOperation("提交建议反馈")
    @PostMapping("/add")
    public JsonResult add(@Validated @RequestBody AdviseFeedbackRequest request) {
        log.info("提交建议反馈/user/member/advise/add,req:{}", JSON.toJSONString(request));
        Long memberId = this.getCurrentMemberId();

        GbMemberAdviseFeedback data = new GbMemberAdviseFeedback();
        data.setTitle(this.trimToEmpty(request.getTitle()));
        data.setContent(this.trimToEmpty(request.getContent()));
        data.setMemberId(memberId);
        // 0-待处理
        data.setStatus(0);

        Long id = service.addAdviseFeedback(data);
        if (id == null || id <= 0) {
            throw new BusinessException(UserErrorCodeEnum.UPDATE_FAILED);
        }
        return JsonResult.success("提交成功", id);
    }

    // 修改建议反馈(仅本人且待处理的记录可改)
    @ApiOperation("修改建议反馈")
    @PostMapping("/edit")
    public JsonResult edit(@Validated @RequestBody AdviseFeedbackRequest request) {
        log.info("修改建议反馈/user/member/advise/edit,req:{}", JSON.toJSONString(request));
        Long memberId = this.getCurrentMemberId();
        if (request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(UserErrorCodeEnum.REQUEST_PARAM_ILLEGAL);
        }
        // 越权校验: 记录必须存在且属于当前用户
        GbMemberAdviseFeedback old = service.getAdviseFeedbackById(request.getId());
        if (ObjectUtils.isEmpty(old) || !service.isOwner(old, memberId)) {
            throw new BusinessException(UserErrorCodeEnum.ADVISE_FEEDBACK_NOT_EXIST);
        }
        // 已受理(非待处理)的记录不允许再修改
        if (old.getStatus() == null || old.getStatus() != 0) {
            throw new BusinessException(UserErrorCodeEnum.ADVISE_FEEDBACK_NOT_ALLOW_EDIT);
        }

        GbMemberAdviseFeedback data = new GbMemberAdviseFeedback();
        data.setId(request.getId());
        data.setTitle(this.trimToEmpty(request.getTitle()));
        data.setContent(this.trimToEmpty(request.getContent()));
        boolean flag = service.editAdviseFeedback(memberId, data);
        if (!flag) {
            log.warn("修改建议反馈失败, memberId:{}, id:{}", memberId, request.getId());
            throw new BusinessException(UserErrorCodeEnum.UPDATE_FAILED);
        }
        return JsonResult.success("修改成功");
    }

    // 删除建议反馈(仅本人记录可删)
    @ApiOperation("删除建议反馈")
    @PostMapping("/del")
    public JsonResult del(@RequestBody AdviseFeedbackRequest request) {
        log.info("删除建议反馈/user/member/advise/del,req:{}", JSON.toJSONString(request));
        Long memberId = this.getCurrentMemberId();
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(UserErrorCodeEnum.REQUEST_PARAM_ILLEGAL);
        }
        boolean flag = service.deleteAdviseFeedback(memberId, request.getId());
        if (!flag) {
            log.warn("删除建议反馈失败, memberId:{}, id:{}", memberId, request.getId());
            throw new BusinessException(UserErrorCodeEnum.ADVISE_FEEDBACK_NOT_EXIST);
        }
        return JsonResult.success("删除成功");
    }

    // 建议反馈详情
    @ApiOperation("建议反馈详情")
    @GetMapping("/info")
    public JsonResult info(@RequestParam("id") Long id) {
        log.info("建议反馈详情/user/member/advise/info,id:{}", id);
        Long memberId = this.getCurrentMemberId();
        if (id == null || id <= 0) {
            throw new BusinessException(UserErrorCodeEnum.REQUEST_PARAM_ILLEGAL);
        }
        GbMemberAdviseFeedback result = service.getAdviseFeedbackById(id);
        // 越权校验: 记录不存在或不属于当前用户时, 统一按不存在返回
        if (ObjectUtils.isEmpty(result) || !service.isOwner(result, memberId)) {
            throw new BusinessException(UserErrorCodeEnum.ADVISE_FEEDBACK_NOT_EXIST);
        }
        return JsonResult.success(new AdviseFeedbackResponse(result));
    }

    // 我的建议反馈列表(分页, 可按状态过滤)
    @ApiOperation("我的建议反馈列表")
    @GetMapping("/list")
    public JsonResult list(@RequestParam(value = "status", required = false) Integer status,
                           @RequestParam(value = "page", required = false) Integer page,
                           @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        log.info("我的建议反馈列表/user/member/advise/list,status:{},page:{},pageSize:{}", status, page, pageSize);
        Long memberId = this.getCurrentMemberId();
        // 请求参数矫正: page 默认1, pageSize 默认10最大20
        int pageNo = Optional.ofNullable(page).orElse(1);
        if (pageNo < 1) pageNo = 1;
        int size = Optional.ofNullable(pageSize).map(v -> Math.min(v, 20)).orElse(10);
        if (size < 1) size = 10;

        List<GbMemberAdviseFeedback> result = service.getAdviseFeedbackList(memberId, status, pageNo, size);
        // 总条数(分页用, 过滤条件与列表一致)
        Long total = service.getAdviseFeedbackCount(memberId, status);
        Map<String, Object> data = new HashMap<>();
        data.put("total", total == null ? 0 : total);
        data.put("list", AdviseFeedbackResponse.getAdviseFeedbackResponseList(result));
        return JsonResult.success(data);
    }

    // 从请求头 Token 中解析当前登录用户id(未登录/无 token 直接抛业务异常)
    private Long getCurrentMemberId() {

        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            throw new BusinessException(UserErrorCodeEnum.TOKEN_NOT_EXIST);
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            throw new BusinessException(UserErrorCodeEnum.USER_NOT_EXIST);
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new BusinessException(UserErrorCodeEnum.USER_NOT_EXIST);
        }
        if (memberId == 0) {
            throw new BusinessException(UserErrorCodeEnum.USER_NOT_EXIST);
        }
        return memberId;
    }

    private String trimToEmpty(String value) {

        return value == null ? "" : value.trim();
    }

}
