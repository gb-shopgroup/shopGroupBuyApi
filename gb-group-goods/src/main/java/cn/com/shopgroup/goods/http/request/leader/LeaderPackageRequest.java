package cn.com.shopgroup.goods.http.request.leader;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LeaderPackageRequest {

    // 包装id,主键自增
    private Long id;

    // 商品id
    private Long gid;

    // 包装名称
    //@NotNull(message = "名称不能为空")
    private String name;

    // 销售价格
    @NotNull(message = "价格不能为空")
    private Double price;

    // 市场价格
    private Double price2;

    // 包装数量
    @NotNull(message = "数量不能为空")
    private Integer num;

}
