package cn.com.shopgroup.goods.http.response.leader;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品信息查询响应
 * 返回商品基本信息(含规格/分类名) + 商品SKU列表
 */
@Data
public class GoodsDetailResponse {

    // 商品信息(含规格/图片/分类名等)
    private LeaderGoodsResponse goods;

    // 商品SKU列表
    private List<LeaderSkuResponse> skuList = new ArrayList<>();
}
