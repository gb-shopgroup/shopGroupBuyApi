package cn.com.shopgroup.goods.http.request.leader;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LeaderAddSpecValRequest {

    // 规格值id
    private Long valId;
    // 规格id
    private Long sid;
    // 规格值
    @NotNull(message = "名称不能为空")
    private String val;

}
