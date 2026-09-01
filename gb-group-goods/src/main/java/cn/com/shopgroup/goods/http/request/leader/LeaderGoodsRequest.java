package cn.com.shopgroup.goods.http.request.leader;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class LeaderGoodsRequest {

    // 商品id,主键自增
    private Long id;

    // 分类id,外键
    @NotNull(message = "分类别不能为空")
    private Long catId;
    // 商品类型,1普通商品2称重商品
    private Byte type;
    // 商品名称
    @NotNull(message = "名称不能为空")
    private String name;
    // 进货价格
    private Double costPrice;
    // 销售价格
    private Double price;
    // 市场价格--划价
    private Double price2;

    // 是否启用库存, 1=启用
    private Byte isStock;
    // 商品库存,总库存
    private Integer stockNum;
    // 是否设置限购
    private Byte isLimit;
    // 限购数量
    private Integer limitNum;
    // 商品单位
    private String unit;
    // 商品介绍
    private String goodsInfo;
    // 商品图片
    @NotNull(message = "图片不能为空")
    private String img;
    private String img2;
    private String img3;
    //规格
    private List<LeaderAddSpecRequest> addSpecList;
    // SKU列表(修改商品时随商品信息一并重建gb_goods_sku_info)
    private List<LeaderSkuRequest> skuList;
}
