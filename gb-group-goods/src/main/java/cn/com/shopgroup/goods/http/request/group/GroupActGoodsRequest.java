package cn.com.shopgroup.goods.http.request.group;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GroupActGoodsRequest {

    // 商品id
    @NotNull(message = "商品不能为空")
    private Long gid;
    // 商品名称,冗余
    private String gname;
    // 商品类型,冗余,1普通商品2称重商品
    private Byte gtype;
    // 商品主图(来源于商品图片)
    private String img;
    // 团购价格(来源于商品价格)
    private Double price;
    // 市场价格(来源于商品价格)
    private Double price2;


}
