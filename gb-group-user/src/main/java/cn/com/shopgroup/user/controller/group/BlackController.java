package cn.com.shopgroup.user.controller.group;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TokenUtils;
import cn.com.shopgroup.user.service.GbMemberBlackListService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/user")
public class BlackController {

    @Resource
    private GbMemberBlackListService service;
    //登录查询是否是（leaderId）团长下的黑名单
    @GetMapping("/group/black")
    public JsonResult groupBlack(@RequestParam("lid") Long leaderId) {
        log.info("[get] /group/black param:leaderId:{}", leaderId);
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

        // 查询是否加入黑名单
        boolean isBlack = service.getMemberBlackById(leaderId, memberId);
        return JsonResult.success(isBlack);
    }


}
