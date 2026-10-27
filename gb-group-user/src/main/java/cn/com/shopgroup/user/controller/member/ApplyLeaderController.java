package cn.com.shopgroup.user.controller.member;

import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.user.exception.UserErrorCodeEnum;
import cn.com.shopgroup.user.http.request.ApplyLeaderRequest;
import cn.com.shopgroup.user.model.GbApplyLeaderRecord;
import cn.com.shopgroup.user.service.GbApplyLeaderRecordService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 申请团长(会员端提交申请记录)
 */
@RestController
@RequestMapping("user/apply")
@Slf4j
public class ApplyLeaderController {

    @Resource
    private GbApplyLeaderRecordService applyLeaderRecordService;

    // 提交申请团长
    @PostMapping("/become/leader")
    public JsonResult applyBecomeLeader(@Validated @RequestBody ApplyLeaderRequest request) {
        log.info("申请团长/user/apply/become/leader,req:{}", JSON.toJSONString(request));

        String name = request.getName() == null ? "" : request.getName().trim();
        String mobile = request.getMobile() == null ? "" : request.getMobile().trim();
        if (StringUtils.isEmpty(name)) {
            throw new BusinessException(UserErrorCodeEnum.REQUEST_PARAM_ILLEGAL);
        }
        // 手机号格式校验(11位大陆手机号)
        if (!mobile.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(UserErrorCodeEnum.REQUEST_PARAM_ILLEGAL);
        }

        // 手机号唯一: 已提交过申请则不允许重复提交
        GbApplyLeaderRecord exists = applyLeaderRecordService.getApplyLeaderRecordByMobile(mobile);
        if (exists != null && exists.getId() != null && exists.getId() > 0) {
            throw new BusinessException(UserErrorCodeEnum.APPLY_LEADER_EXISTED);
        }

        GbApplyLeaderRecord data = new GbApplyLeaderRecord();
        data.setName(name);
        data.setMobile(mobile);
        // 0=申请中
        data.setApplyStatus(0);

        Long id = 0L;
        try {
            id = applyLeaderRecordService.addApplyLeaderRecord(data);
        } catch (DataIntegrityViolationException e) {
            // 并发场景下手机号唯一键冲突(查重通过后仍有极小概率撞键), 转为友好提示返回
            log.warn("申请团长重复提交, 手机号唯一键冲突, mobile:{}", mobile);
            throw new BusinessException(UserErrorCodeEnum.APPLY_LEADER_EXISTED);
        }

        if (id == null || id <= 0) {
            throw new BusinessException(UserErrorCodeEnum.UPDATE_FAILED);
        }
        return JsonResult.success("提交成功", id);
    }

}
