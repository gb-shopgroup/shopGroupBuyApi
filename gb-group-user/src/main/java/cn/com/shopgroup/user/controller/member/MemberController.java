package cn.com.shopgroup.user.controller.member;

import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.IntEncryptorUtils;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TokenUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.user.http.response.LeaderResponse;
import cn.com.shopgroup.user.http.response.LoginMemberResponse;
import cn.com.shopgroup.user.model.GbMemberInfo;
import cn.com.shopgroup.user.model.GbOrgLeaderInfo;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.model.GbOrgShopInfo;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import cn.com.shopgroup.user.service.GbMemberInfoService;
import cn.com.shopgroup.user.service.GbOrgLeaderInfoService;
import cn.com.shopgroup.user.service.GbOrgShopInfoService;
import cn.com.shopgroup.user.service.GbOrgStaffInfoService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("user/member")
@Slf4j
public class MemberController {

    @Resource
    private GbMemberInfoService service;

    @Resource
    private GbOrgLeaderInfoService leaderService;

    @Resource
    private GbOrgStaffInfoService staffService;

    @Resource
    private GbOrgShopInfoService shopService;

    @Resource
    private RedisHelper redisHelper;

    @Resource
    private WxMiniAccessTokenHelper wxMiniAccessTokenHelper;


    // 根据 token 获取用户信息
    @GetMapping("/info")
    public JsonResult info() {

        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            return JsonResult.fail("token不存在");
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            return JsonResult.fail("用户不存在");
        }
        Long memberId = 0l;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return JsonResult.fail("用户不存在");
        }
        if (memberId == 0) {
            return JsonResult.fail("用户不存在");
        }

        // 查询用户信息
        GbMemberInfo result = service.getMiniMemberById(memberId);
        if (result == null) {
            return JsonResult.fail("用户不存在");
        }
        LoginMemberResponse data = new LoginMemberResponse(result);

        // 生成token
        String token2 = TokenUtils.createToken(String.valueOf(result.getMemberId()));
        // 设置token
        data.setToken(token2);

        // 返回数据
        return JsonResult.success(data);
    }

    // 是否团长身份
    @GetMapping("/isleader")
    public JsonResult getIsLeader() {

        // 查询用户信息
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            return JsonResult.fail("token不存在");
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || StringUtils.isEmpty(userId) || userId.matches("^[0-9]+$") == false) {
            return JsonResult.fail("用户不存在");
        }
        Long memberId = 0L;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return JsonResult.fail("用户不存在");
        }
        if (memberId == 0) {
            return JsonResult.fail("用户不存在");
        }

        // 查询用户openid
        GbMemberInfo info = service.getMemberInfo(memberId);
        if (info == null) return JsonResult.fail("用户不存在");
        String openid = info.getOpenid();
        if (openid == null || openid.length() == 0) {
            return JsonResult.fail("用户不存在");
        }

        // 返回团长员工信息
        LeaderResponse leader = new LeaderResponse();

        // 先查询员工信息
        GbOrgStaffInfo staffInfo = staffService.getMiniStaffInfo(openid);
        if (staffInfo != null && staffInfo.getLeaderId() > 0) {

            // 团长员工信息
            leader.setLid(IntEncryptorUtils.encrypt(staffInfo.getLeaderId().intValue()));
            leader.setSid(IntEncryptorUtils.encrypt(staffInfo.getStaffId().intValue()));
            leader.setName(staffInfo.getStaffName());
            leader.setIsSuper(false);
            leader.setAuth(staffInfo.getAuthList());

            // 团长店铺名称
            GbOrgShopInfo shopInfo = shopService.getMiniLeaderShop(staffInfo.getLeaderId());
            log.info("是否团长身份接口 leaderId:{},shop:{}", staffInfo.getLeaderId(), JSON.toJSONString(shopInfo));
            if (ObjectUtils.isEmpty(shopInfo)) {
                leader.setShop(shopInfo.getShopName());
            }

            // 查询员工负责的提货点
            List<GbOrgPointInfo> lists = staffService.getMiniStaffPointList(staffInfo.getStaffId());
            if (CollectionUtils.isEmpty(lists)) {
                lists = new ArrayList<>();
            }
            List<Long> pointList = lists.stream().map(GbOrgPointInfo::getPointId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(pointList)) {
                pointList = new ArrayList<>();
            }
            String pointIds = pointList.stream().map(String::valueOf).collect(Collectors.joining(","));
            if (StringUtils.isEmpty(pointIds)) {
                pointIds = "";
            }
            leader.setPointIds(pointIds);
        }

        // 根据openid查询是否团长
        GbOrgLeaderInfo leaderInfo = leaderService.getMiniLeaderInfo(openid);
        if (!ObjectUtils.isEmpty(leaderInfo) && leaderInfo.getLeaderId() > 0) {
            leader.setLid(IntEncryptorUtils.encrypt(leaderInfo.getLeaderId().intValue()));
            leader.setIsSuper(true);
        }

        // 返回团长员工信息
        return JsonResult.success(leader);
    }


}
