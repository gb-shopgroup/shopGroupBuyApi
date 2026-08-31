package cn.com.shopgroup.goods.http.request.leader;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SpecValRequest {

    // 规格值id
    private Long id;
    // 规格id
    private Long sid;
    //商品id
    private Long gid;
    // 规格值
    @NotNull(message = "名称不能为空")
    private String val;

}
