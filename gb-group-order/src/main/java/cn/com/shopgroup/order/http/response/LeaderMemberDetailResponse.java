package cn.com.shopgroup.order.http.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 团长端-团员详情
 */
@Data
@ApiModel("团员详情")
public class LeaderMemberDetailResponse {

    @ApiModelProperty("用户id")
    private Long memberId;

    @ApiModelProperty("脱敏手机号")
    private String mobile;

    @ApiModelProperty("用户昵称")
    private String nickname;

    @ApiModelProperty("用户头像")
    private String avatar;

    @ApiModelProperty("消费总额，单位元")
    private String consumeAmount;

    @ApiModelProperty("退款金额，单位元")
    private String refundAmount;

    @ApiModelProperty("跟团次数")
    private Integer orderCount;

    @ApiModelProperty("查看次数（当前无足迹表时返回0）")
    private Integer viewCount;

    @ApiModelProperty("动态分组列表")
    private List<MemberDynamicGroup> dynamicList;

    public LeaderMemberDetailResponse() {
    }

    public LeaderMemberDetailResponse(Long memberId, String mobile, String nickname, String avatar,
                                      String consumeAmount, String refundAmount, Integer orderCount,
                                      Integer viewCount, List<MemberDynamicGroup> dynamicList) {
        this.memberId = memberId;
        this.mobile = mobile;
        this.nickname = nickname;
        this.avatar = avatar;
        this.consumeAmount = consumeAmount;
        this.refundAmount = refundAmount;
        this.orderCount = orderCount;
        this.viewCount = viewCount;
        this.dynamicList = dynamicList;
    }
}
