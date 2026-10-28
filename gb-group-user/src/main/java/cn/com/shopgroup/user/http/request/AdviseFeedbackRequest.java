package cn.com.shopgroup.user.http.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@ApiModel("用户建议反馈请求")
public class AdviseFeedbackRequest {

    // 反馈记录id(新增时不传, 修改/删除时必传)
    @ApiModelProperty("反馈记录id")
    private Long id;

    // 反馈标题
    @NotNull(message = "反馈标题不能为空")
    @Size(max = 100, message = "反馈标题不能超过100个字符")
    @ApiModelProperty("反馈标题")
    private String title;

    // 反馈内容
    @NotNull(message = "反馈内容不能为空")
    @ApiModelProperty("反馈内容")
    private String content;

}
