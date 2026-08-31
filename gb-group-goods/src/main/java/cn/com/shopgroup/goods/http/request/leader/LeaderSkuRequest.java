package cn.com.shopgroup.goods.http.request.leader;

import lombok.Data;

@Data
public class LeaderSkuRequest {

    // skuid
    private Long id;
    // 商品id
    private Long gid;
    // skuids, 规格1+规格2+......
    private String ids;
    // sku名称, 规格1+规格2+......
    private String names;
    // 销售价格
    private Double price;
    // 市场价格
    private Double price2;
    // 商品库存
    private Integer num;
    // 商品图片
    private String img;

}
