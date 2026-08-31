package cn.com.shopgroup.goods.http.request.leader;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LeaderGoodsStockRequest {

    @NotNull(message = "商品不能为空")
    private Long gid;    // 商品id

    @NotNull(message = "数量不能为空")
    private Integer gnum;   // 调整数量

    @NotNull(message = "类型不能为空")
    private Byte gtype; // 1增加2减少

}
