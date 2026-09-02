package cn.com.shopgroup.order.http.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 真实跟团记录响应
 */
@Data
@ApiModel("跟团记录")
public class GroupOrderRecordResponse {

    @ApiModelProperty("用户编号")
    private Long memberId;

    @ApiModelProperty("脱敏手机号，只展示前2位和后4位，中间用*号代替")
    private String mobile;

    @ApiModelProperty("用户头像")
    private String avatar;

    @ApiModelProperty("用户昵称")
    private String nickname;

    @ApiModelProperty("相对时间，如：刚刚、1分钟前")
    private String time;

    @ApiModelProperty("商品规格描述")
    private String goodsDesc;

    @ApiModelProperty("购买数量")
    private Integer goodsNum;

    public GroupOrderRecordResponse() {
    }

    public GroupOrderRecordResponse(Long memberId, String mobile, String avatar, String nickname, String time, String goodsDesc, Integer goodsNum) {
        this.memberId = memberId;
        this.mobile = mobile;
        this.avatar = avatar;
        this.nickname = nickname;
        this.time = time;
        this.goodsDesc = goodsDesc;
        this.goodsNum = goodsNum;
    }
}
