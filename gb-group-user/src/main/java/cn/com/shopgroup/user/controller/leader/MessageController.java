package cn.com.shopgroup.user.controller.leader;

import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.user.exception.UserErrorCodeEnum;
import cn.com.shopgroup.user.http.response.MessageResponse;
import cn.com.shopgroup.user.model.GbOrgLeaderInfo;
import cn.com.shopgroup.user.model.GbOrgMessageInfo;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import cn.com.shopgroup.user.service.GbOrgLeaderInfoService;
import cn.com.shopgroup.user.service.GbOrgMessageInfoService;
import cn.com.shopgroup.user.service.GbOrgStaffInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user")
public class MessageController {

    @Resource
    private GbOrgMessageInfoService orgMessageInfoService;

    @Resource
    private GbOrgLeaderInfoService orgLeaderInfoService;

    @Resource
    private GbOrgStaffInfoService orgStaffInfoService;

    // 消息列表(团长查看所有消息)
    @GetMapping("/leader/message/list")
    public JsonResult messageList(@RequestParam("type") Integer msgType, @RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 团长查看所有消息：员工的openid和团长openid是否一致
        GbOrgLeaderInfo leaderInfo = orgLeaderInfoService.getLeaderInfo(leaderId);
        if (ObjectUtils.isEmpty(leaderInfo)) {
            throw new BusinessException("lid=" + leaderId + UserErrorCodeEnum.DATA_NOT_FOUND.getMessage());
        }
        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        GbOrgStaffInfo staffInfo = null;
        if (staffId != 0) {
            staffInfo = orgStaffInfoService.getStaffInfo(staffId);
            if (ObjectUtils.isEmpty(staffInfo)) {
                throw new BusinessException(UserErrorCodeEnum.STAFF_NOT_EXIST);
            }
        }

        // 请求参数矫正
        if (page == 0) page = 1;
        if (pageSize == 0) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        if (staffInfo != null) {
            if (leaderInfo.getOpenid().equalsIgnoreCase(staffInfo.getOpenid())) {
                staffId = 0l; // 团长查看所有消息
            }
        }
        // 消息列表查询
        List<GbOrgMessageInfo> result = orgMessageInfoService.getMiniLeaderMessageList(leaderId, staffId, msgType, page, pageSize);
        List<MessageResponse> data = MessageResponse.getMessageResponseList(result);
        return JsonResult.success(data);
    }

    // 消息列表总数量(团长查看所有消息)
    @GetMapping("/leader/message/count")
    public JsonResult messageCount(@RequestParam("type") Integer msgType) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 团长查看所有消息：员工的openid和团长openid是否一致
        GbOrgLeaderInfo leaderInfo = orgLeaderInfoService.getLeaderInfo(leaderId);
        if (ObjectUtils.isEmpty(leaderInfo)) {
            throw new BusinessException("lid=" + leaderId + UserErrorCodeEnum.DATA_NOT_FOUND.getMessage());
        }
        // 从请求头中获取员工id
        GbOrgStaffInfo staffInfo = null;
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        if (staffId != 0) {
            staffInfo = orgStaffInfoService.getStaffInfo(staffId);
            if (ObjectUtils.isEmpty(staffInfo)) {
                throw new BusinessException(UserErrorCodeEnum.STAFF_NOT_EXIST);
            }
        }

        // 团长查看所有消息：员工的openid和团长openid是否一致
        if (staffInfo != null) {
            if (leaderInfo.getOpenid().equalsIgnoreCase(staffInfo.getOpenid())) {
                staffId = 0l; // 团长查看所有消息
            }
        }

        // 查询数量
        long total = orgMessageInfoService.getMiniLeaderMessageCount(leaderId, staffId, msgType);
        return JsonResult.success(total);
    }

    // 未读消息总数量(团长查看所有消息)
    @GetMapping("/leader/message/unread")
    public JsonResult messageunRead(@RequestParam("type") Integer msgType) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);

        // 团长查看所有消息：员工的openid和团长openid是否一致
        GbOrgLeaderInfo leaderInfo = orgLeaderInfoService.getLeaderInfo(leaderId);
        if (ObjectUtils.isEmpty(leaderInfo)) {
            throw new BusinessException("lid=" + leaderId + UserErrorCodeEnum.DATA_NOT_FOUND.getMessage());
        }
        // 从请求头中获取员工id
        GbOrgStaffInfo staffInfo = null;
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        if (staffId != 0) {
            staffInfo = orgStaffInfoService.getStaffInfo(staffId);
            if (ObjectUtils.isEmpty(staffInfo)) {
                throw new BusinessException(UserErrorCodeEnum.DATA_NOT_FOUND);
            }
        }

        // 团长查看所有消息：员工的openid和团长openid是否一致
        if (staffInfo != null) {
            if (leaderInfo.getOpenid().equalsIgnoreCase(staffInfo.getOpenid())) {
                staffId = 0l; // 团长查看所有消息
            }
        }

        // 查询数量
        Long total = orgMessageInfoService.getMiniLeaderMessageUnReadCount(leaderId, staffId, msgType);
        return JsonResult.success(total);
    }

    // 阅读消息
    @GetMapping("/leader/message/read")
    public JsonResult readMessage(@RequestParam("id") Long msgId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);

        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        if (staffId != 0) {
            GbOrgStaffInfo staffInfo = orgStaffInfoService.getStaffInfo(staffId);
            if (ObjectUtils.isEmpty(staffInfo)) {
                throw new BusinessException(UserErrorCodeEnum.STAFF_NOT_EXIST);
            }
        }

        // 设置阅读
        orgMessageInfoService.readMiniLeaderMessageInfo(leaderId, staffId, msgId);
        return JsonResult.success();
    }


}
