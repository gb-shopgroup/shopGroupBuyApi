package cn.com.shopgroup.order.http.request;

import lombok.Data;

@Data
public class OrderGoodsRequest {

    // 商品ID
    private Long id;

    // 商品数量
    private Integer num;

    // 包装信息
    private Long packId = 0L;
    private String packName = "";
    private Integer packNum = 0;

    // sku信息
    private Long skuId = 0L;
    private String skuids = "";
    private String skunames = "";

}
