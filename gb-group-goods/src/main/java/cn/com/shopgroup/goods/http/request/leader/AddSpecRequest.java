package cn.com.shopgroup.goods.http.request.leader;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AddSpecRequest {
    // 规格名称
    @NotNull(message = "名称不能为空")
    private String name;
    // 是否设置价格
    private Byte price;
    // 是否设置库存
    private Byte stock;
    //商品id（没有就传0）
    private Long goodId;

}
