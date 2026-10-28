package cn.com.shopgroup.http.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("建议反馈回复请求")
public class AdviseReplyRequest {

    // 反馈记录id
    @NotNull(message = "反馈id不能为空")
    @ApiModelProperty("反馈记录id")
    private Long id;

    // 官方回复内容
    @NotNull(message = "回复内容不能为空")
    @ApiModelProperty("官方回复内容")
    private String reply;

    // 处理状态: 0-待处理, 1-处理中, 2-已解决, 3-已关闭(不传表示保持原状态)
    @ApiModelProperty("处理状态: 0-待处理, 1-处理中, 2-已解决, 3-已关闭")
    private Integer status;

}
