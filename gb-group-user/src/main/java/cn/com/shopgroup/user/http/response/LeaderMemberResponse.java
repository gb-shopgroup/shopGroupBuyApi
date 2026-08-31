package cn.com.shopgroup.user.http.response;

import cn.com.shopgroup.user.model.GbMemberBlackList;
import cn.com.shopgroup.user.model.GbMemberInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LeaderMemberResponse {

    // 用户id
    private Long id;
    // 手机号码
    private String mobile;
    // 微信昵称
    private String nickname;
    // 微信头像
    private String avatar;

    public LeaderMemberResponse() {

    }

    public LeaderMemberResponse(Long id, String mobile, String nickname, String avatar) {

        this.id = id;
        this.mobile = mobile;
        this.nickname = nickname;
        this.avatar = avatar;
    }

    public LeaderMemberResponse(GbMemberInfo data) {

        this.id = data.getMemberId();
        this.mobile = data.getMobile();
        this.nickname = data.getNickname();
        this.avatar = data.getAvatar();
    }

    public LeaderMemberResponse(GbMemberBlackList data) {

        this.id = data.getMemberId();
        this.mobile = data.getMobile();
        this.nickname = data.getNickName();
        this.avatar = data.getAvatar();
    }

    // 列表转化
    public static List<LeaderMemberResponse> getMemberResponseList(List<GbMemberBlackList> lists) {

        List<LeaderMemberResponse> data = new ArrayList<>();
        for (GbMemberBlackList item : lists) {
            data.add(new LeaderMemberResponse(item));
        }
        return data;
    }


}
