package cn.com.shopgroup.goods.http.request.leader;

import lombok.Data;

@Data
public class UpdateSkuStockRequest {
    //skuId
    private Long skuId;
    // 库存
    private Integer num;

}
