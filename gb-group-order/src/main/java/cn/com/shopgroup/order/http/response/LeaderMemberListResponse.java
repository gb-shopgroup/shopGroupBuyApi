package cn.com.shopgroup.order.http.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 团长端-我的团员列表项
 */
@Data
@ApiModel("团员列表项")
public class LeaderMemberListResponse {

    @ApiModelProperty("用户id")
    private Long memberId;

    @ApiModelProperty("脱敏手机号")
    private String mobile;

    @ApiModelProperty("用户昵称")
    private String nickname;

    @ApiModelProperty("用户头像")
    private String avatar;

    @ApiModelProperty("最近动态时间描述，如：20分钟前")
    private String lastTimeDesc;

    @ApiModelProperty("最近动态内容，如：查看了XXXX团 / 跟团下单 XXXX团")
    private String lastActionDesc;

    @ApiModelProperty("消费总额，单位元")
    private String consumeAmount;

    @ApiModelProperty("跟团次数")
    private Integer orderCount;

    @ApiModelProperty("查看次数（当前无足迹表时返回0）")
    private Integer viewCount;

    public LeaderMemberListResponse() {
    }

    public LeaderMemberListResponse(Long memberId, String mobile, String nickname, String avatar,
                                    String lastTimeDesc, String lastActionDesc, String consumeAmount,
                                    Integer orderCount, Integer viewCount) {
        this.memberId = memberId;
        this.mobile = mobile;
        this.nickname = nickname;
        this.avatar = avatar;
        this.lastTimeDesc = lastTimeDesc;
        this.lastActionDesc = lastActionDesc;
        this.consumeAmount = consumeAmount;
        this.orderCount = orderCount;
        this.viewCount = viewCount;
    }
}
