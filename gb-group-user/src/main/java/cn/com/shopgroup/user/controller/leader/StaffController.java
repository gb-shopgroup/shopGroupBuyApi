package cn.com.shopgroup.user.controller.leader;

import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.user.exception.UserErrorCodeEnum;
import cn.com.shopgroup.user.http.request.StaffRequest;
import cn.com.shopgroup.user.http.response.StaffResponse;
import cn.com.shopgroup.user.model.GbMemberInfo;
import cn.com.shopgroup.user.model.GbOrgLeaderInfo;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import cn.com.shopgroup.user.service.GbMemberInfoService;
import cn.com.shopgroup.user.service.GbOrgLeaderInfoService;
import cn.com.shopgroup.user.service.GbOrgStaffInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/user")
public class StaffController {

    @Resource
    private GbOrgStaffInfoService staffInfoService;

    @Resource
    private GbMemberInfoService memberService;

    @Resource
    private GbOrgLeaderInfoService leaderInfoService;


    // 查询当前团长员工列表(不含超级团长)
    @GetMapping("/leader/staff/list")
    public JsonResult staffList() {

        //从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 查询列表
        List<GbOrgStaffInfo> result = staffInfoService.getMiniLeaderStaffList(leaderId);
        log.info("/leader/staff/list leaderId:{},result:{}", leaderId, JSON.toJSONString(result));
        if (CollectionUtils.isEmpty(result)) {
            return JsonResult.success();
        }
        // 去掉列表中第一个元素，就是超级团长角色
        //result.remove(0);
        List<StaffResponse> data = StaffResponse.getStaffResponseList(result);
        return JsonResult.success(data);
    }

    // 添加员工(校验用户存在且未绑定, 绑定提货点)
    @PostMapping("/leader/staff/add")
    public JsonResult addStaff(@Validated @RequestBody StaffRequest request) {
        log.info("添加员工/leader/staff/add,req:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        GbOrgLeaderInfo leaderInfo = leaderInfoService.getLeaderInfo(leaderId);
        if (ObjectUtils.isEmpty(leaderInfo)) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_INFO_NOT_FIND);
        }

        // 根据手机号检查员工是否在 member_info 表中
        GbMemberInfo memberInfo = memberService.getMemberInfoByMobile(request.getMobile());
        if (memberInfo == null || memberInfo.getMemberId() == 0) {
            throw new BusinessException(UserErrorCodeEnum.STAFF_MUST_BU_MEMBER);
        }


        // 根据openid检查员工是否已经存在
        String openid = memberInfo.getOpenid();
        //团长openid
        String leaderOpenid = leaderInfo.getOpenid();
        if (openid.equals(leaderOpenid)) {
            throw new BusinessException(UserErrorCodeEnum.OPERATION_NOT_SELF);
        }
        GbOrgStaffInfo staffInfo = staffInfoService.getMiniStaffInfo(openid);
        if (staffInfo != null && staffInfo.getStaffId() > 0) {
            throw new BusinessException(UserErrorCodeEnum.STAFF_EXISTED);
        }

        // 添加员工到数据库
        GbOrgStaffInfo data = new GbOrgStaffInfo();
        data.setStaffName(request.getName());
        data.setRemark(request.getRemark());
        data.setAuthList(request.getAuth());

        // 提货点id集合, 逗号间隔的字符串转化为Integer列表
        List<Long> pointIds = Arrays.stream(request.getPoint().split(",")).map(Long::parseLong).collect(Collectors.toList());

        data.setMobile(memberInfo.getMobile());
        data.setNickname(memberInfo.getNickname());
        data.setAvatar(memberInfo.getAvatar());
        data.setOpenid(memberInfo.getOpenid());

        // 返回
        Long staffId = staffInfoService.addMiniLeaderStaff(leaderId, data, pointIds);
        return JsonResult.success("添加成功", staffId);
    }

    // 修改员工信息(支持更换员工及提货点)
    @PostMapping("/leader/staff/edit")
    public JsonResult editStaff(@Validated @RequestBody StaffRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);

        // 查询旧的用户信息
        GbOrgStaffInfo staffInfo = staffInfoService.getStaffInfo(request.getId());
        if (ObjectUtils.isEmpty(staffInfo)) {
            throw new BusinessException(UserErrorCodeEnum.DATA_NOT_FOUND);
        }

        // 新的员工信息
        GbOrgStaffInfo newStaffInfo = new GbOrgStaffInfo();
        newStaffInfo.setStaffId(request.getId());
        newStaffInfo.setStaffName(request.getName());
        newStaffInfo.setRemark(request.getRemark());
        newStaffInfo.setAuthList(request.getAuth());

        // 是否更换员工
        if (staffInfo.getMobile().equalsIgnoreCase(request.getMobile()) == false) {

            // 根据手机号检查员工是否在 member_info 表中
            GbMemberInfo tempMember = memberService.getMemberInfoByMobile(request.getMobile());
            if (tempMember == null || tempMember.getMemberId() == 0) {
                throw new BusinessException(UserErrorCodeEnum.USER_NOT_EXIST);
            }

            // 根据openid检查员工是否已经存在
            GbOrgStaffInfo tempStaff = staffInfoService.getMiniStaffInfo(tempMember.getOpenid());
            if (tempStaff != null && tempStaff.getStaffId() > 0) {
                throw new BusinessException(UserErrorCodeEnum.STAFF_EXISTED);
            }

            // 新的员工信息
            newStaffInfo.setMobile(tempMember.getMobile());
            newStaffInfo.setNickname(tempMember.getNickname());
            newStaffInfo.setAvatar(tempMember.getAvatar());
            newStaffInfo.setOpenid(tempMember.getOpenid());

        } else {
            newStaffInfo.setMobile("");
        }

        // 提货点
        List<Long> pointIds = Arrays.stream(request.getPoint().split(",")).map(Long::parseLong).collect(Collectors.toList());

        // 修改员工
        Boolean flag = staffInfoService.editMiniLeaderStaff(leaderId, newStaffInfo, pointIds);
        return JsonResult.success();
    }

    // 关闭/启用员工账号(不能操作自己)
    @PostMapping("/leader/staff/close")
    public JsonResult closeStaff(@RequestParam("id") Long staffId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);

//        // 从请求头中获取当前员工id
//        Long sid = RequestParamsUtils.getRequestHeaderStaffId();
//        if (sid == 0) {
//            throw new BusinessException(UserErrorCodeEnum.STAFF_NOT_EXIST);
//        }
//
//        // 不能自己关闭自己
//        if (staffId == sid) {
//            throw new BusinessException(UserErrorCodeEnum.OPERATION_NOT_SELF);
//        }

        // 查询旧的用户信息
        GbOrgStaffInfo staffInfo = staffInfoService.getStaffInfo(staffId);
        if (staffInfo.getIsClose() == 0) {
            staffInfoService.closeMiniLeaderStaff(leaderId, staffId, 1);
        } else {
            staffInfoService.closeMiniLeaderStaff(leaderId, staffId, 0);
        }
        return JsonResult.success();
    }

    // 删除员工(不能删除自己)
    @PostMapping("/leader/staff/remove")
    public JsonResult removeStaff(@RequestParam("staffId") Long staffId) {

        throw new BusinessException(UserErrorCodeEnum.NOT_SUPPORT_DELETE_ACTION);
        // 从请求头中获取团长id
//        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
//        if (leaderId == 0) throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
//
//        // 不能自己关闭自己
//        if (staffId == leaderId) {
//            throw new BusinessException(UserErrorCodeEnum.OPERATION_NOT_SELF);
//        }
//        int m = staffInfoService.removeStaff(leaderId, staffId);
//        return JsonResult.success("操作成功");
    }

}
