package cn.com.shopgroup.user.http.response;

import cn.com.shopgroup.user.model.GbMemberAdviseFeedback;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("用户建议反馈")
public class AdviseFeedbackResponse {

    // 时间展示格式
    private static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @ApiModelProperty("反馈记录id")
    private Long id;

    @ApiModelProperty("反馈标题")
    private String title;

    @ApiModelProperty("反馈内容")
    private String content;

    @ApiModelProperty("提交用户id")
    private Long memberId;

    @ApiModelProperty("处理状态: 0-待处理, 1-处理中, 2-已解决, 3-已关闭")
    private Integer status;

    @ApiModelProperty("处理状态名称")
    private String statusName;

    @ApiModelProperty("官方回复内容")
    private String reply;

    @ApiModelProperty("创建时间")
    private String addTime;

    @ApiModelProperty("更新时间")
    private String updateTime;

    public AdviseFeedbackResponse() {

    }

    public AdviseFeedbackResponse(GbMemberAdviseFeedback data) {

        this.id = data.getId();
        this.title = data.getTitle();
        this.content = data.getContent();
        this.memberId = data.getMemberId();
        this.status = data.getStatus();
        this.statusName = getStatusName(data.getStatus());
        this.reply = data.getReply();
        this.addTime = formatTime(data.getAddTime());
        this.updateTime = formatTime(data.getUpdateTime());
    }

    // 列表转化
    public static List<AdviseFeedbackResponse> getAdviseFeedbackResponseList(List<GbMemberAdviseFeedback> lists) {

        List<AdviseFeedbackResponse> data = new ArrayList<>();
        if (lists == null) {
            return data;
        }
        for (GbMemberAdviseFeedback item : lists) {
            data.add(new AdviseFeedbackResponse(item));
        }
        return data;
    }

    // 状态值转名称: 0-待处理, 1-处理中, 2-已解决, 3-已关闭
    public static String getStatusName(Integer status) {

        if (status == null) {
            return "";
        }
        switch (status) {
            case 0:
                return "待处理";
            case 1:
                return "处理中";
            case 2:
                return "已解决";
            case 3:
                return "已关闭";
            default:
                return "";
        }
    }

    // 时间格式化, 避免直接返回 Date 序列化结果不统一
    private static String formatTime(Date time) {

        if (time == null) {
            return "";
        }
        return new SimpleDateFormat(TIME_PATTERN).format(time);
    }

}
