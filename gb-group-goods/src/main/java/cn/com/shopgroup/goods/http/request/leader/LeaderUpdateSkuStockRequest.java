package cn.com.shopgroup.goods.http.request.leader;

import lombok.Data;

import java.util.List;

@Data
public class LeaderUpdateSkuStockRequest {

    // 商品id
    private Long goodsId;
    //skuId-info
    private List<UpdateSkuStockRequest> skuStockRequestList;

}
