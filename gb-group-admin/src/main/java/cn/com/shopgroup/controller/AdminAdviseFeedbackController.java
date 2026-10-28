package cn.com.shopgroup.controller;

import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.exception.AdminErrorCodeEnum;
import cn.com.shopgroup.http.request.AdviseReplyRequest;
import cn.com.shopgroup.user.http.response.AdviseFeedbackResponse;
import cn.com.shopgroup.user.model.GbMemberAdviseFeedback;
import cn.com.shopgroup.user.service.GbMemberAdviseFeedbackService;
import com.alibaba.fastjson2.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 管理后台-用户建议反馈：列表 / 详情 / 官方回复（含状态流转）
 */
@RestController
@Api(value = "管理后台-用户建议反馈", tags = "管理后台-用户建议反馈")
@Slf4j
public class AdminAdviseFeedbackController {

    @Resource
    private GbMemberAdviseFeedbackService service;

    // 建议反馈列表(待处理优先, 支持按状态过滤与标题/内容模糊搜索)
    @ApiOperation("建议反馈列表")
    @GetMapping("/admin/advise/list")
    public JsonResult adviseList(@RequestParam(value = "status", required = false) Integer status,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "page", required = false) Integer page,
                                 @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        log.info("[get] /admin/advise/list status:{},keyword:{},page:{},pageSize:{}", status, keyword, page, pageSize);
        int pageNo = Optional.ofNullable(page).orElse(1);
        if (pageNo < 1) pageNo = 1;
        int size = Optional.ofNullable(pageSize).map(v -> Math.min(v, 20)).orElse(10);
        if (size < 1) size = 10;

        List<GbMemberAdviseFeedback> result = service.getAdminAdviseFeedbackList(status, keyword, pageNo, size);
        if (CollectionUtils.isEmpty(result)) {
            return JsonResult.success(new ArrayList<AdviseFeedbackResponse>());
        }
        return JsonResult.success(AdviseFeedbackResponse.getAdviseFeedbackResponseList(result));
    }

    // 建议反馈总数(与列表筛选条件一致, 供分页)
    @ApiOperation("建议反馈总数")
    @GetMapping("/admin/advise/count")
    public JsonResult adviseCount(@RequestParam(value = "status", required = false) Integer status,
                                  @RequestParam(value = "keyword", required = false) String keyword) {

        Long total = service.getAdminAdviseFeedbackCount(status, keyword);
        return JsonResult.success(total == null ? 0 : total);
    }

    // 建议反馈详情
    @ApiOperation("建议反馈详情")
    @GetMapping("/admin/advise/info")
    public JsonResult adviseInfo(@RequestParam("id") Long id) {

        if (id == null || id <= 0) {
            throw new BusinessException(AdminErrorCodeEnum.ADVISE_ID_EMPTY);
        }
        GbMemberAdviseFeedback result = service.getAdviseFeedbackById(id);
        if (ObjectUtils.isEmpty(result)) {
            throw new BusinessException(AdminErrorCodeEnum.ADVISE_NOT_FOUND);
        }
        return JsonResult.success(new AdviseFeedbackResponse(result));
    }

    // 官方回复(可同时流转处理状态: 0-待处理, 1-处理中, 2-已解决, 3-已关闭)
    @ApiOperation("官方回复建议反馈")
    @PostMapping("/admin/advise/reply")
    public JsonResult adviseReply(@Validated @RequestBody AdviseReplyRequest request) {
        log.info("官方回复/admin/advise/reply,req:{}", JSON.toJSONString(request));

        String reply = request.getReply() == null ? "" : request.getReply().trim();
        if (reply.length() == 0) {
            throw new BusinessException(AdminErrorCodeEnum.ADVISE_REPLY_EMPTY);
        }
        // 状态流转: 不传保持原状态, 传值时校验取值范围
        Integer status = request.getStatus();
        if (status != null && (status < 0 || status > 3)) {
            throw new BusinessException(AdminErrorCodeEnum.ADVISE_STATUS_ILLEGAL);
        }
        // 记录必须存在
        GbMemberAdviseFeedback old = service.getAdviseFeedbackById(request.getId());
        if (ObjectUtils.isEmpty(old)) {
            throw new BusinessException(AdminErrorCodeEnum.ADVISE_NOT_FOUND);
        }

        boolean flag = service.replyAdviseFeedback(request.getId(), reply, status);
        if (!flag) {
            throw new BusinessException(AdminErrorCodeEnum.UPDATE_FAILED);
        }
        return JsonResult.success("回复成功");
    }

}
