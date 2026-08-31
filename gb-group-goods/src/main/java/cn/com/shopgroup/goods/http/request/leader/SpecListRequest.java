package cn.com.shopgroup.goods.http.request.leader;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SpecListRequest {

    // 商品id
    @NotNull(message = "商品id不能为空")
    private Long gid;

    // 包装列表
    @NotEmpty(message = "规格列表不能为空")
    private List<SpecRequest> lists;

}
