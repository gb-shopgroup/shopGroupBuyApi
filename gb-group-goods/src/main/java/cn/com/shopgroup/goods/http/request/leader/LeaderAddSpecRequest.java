package cn.com.shopgroup.goods.http.request.leader;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class LeaderAddSpecRequest {

    // 规格id
    private Long specId;
    // 规格名称
    @NotNull(message = "名称不能为空")
    private String name;
    // 是否设置价格
    private Byte price;
    // 是否设置库存
    private Byte stock;

    // 规格值列表
    private List<LeaderAddSpecValRequest> specValLists;

}
