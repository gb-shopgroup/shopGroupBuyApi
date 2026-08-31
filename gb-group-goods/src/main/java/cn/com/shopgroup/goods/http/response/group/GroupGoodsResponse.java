package cn.com.shopgroup.goods.http.response.group;

import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGoodsPackageInfo;
import cn.com.shopgroup.goods.model.GbGoodsSkuInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecInfo;
import lombok.Data;

import java.util.List;

@Data
public class GroupGoodsResponse {

    // 商品ID
    private Long id;
    // 商品名称
    private String name;
    // 商品主图
    private String img;
    // 商品价格
    private Double price;
    private Double price2;

    // 商品类型,1普通商品2称重商品
    private Byte type;

    // 是否设置库存
    private Byte stock;
    //库存数量
    private  Integer num;
    // 商品单位
    private String unit;

    // 是否限购
    private Byte limit;
    // 限购数量
    private Integer quantity;

    // 商品包装列表
    private List<GroupPackageResponse> packs;

    // 商品规格列表
    private List<GroupSpecResponse> specs;

    // 商品sku价格和库存
    private List<GroupSkuResponse> skus;

    public GroupGoodsResponse(){

    }

    public GroupGoodsResponse(GbGoodsInfo info, List<GbGoodsPackageInfo> packList, List<GbGoodsSpecInfo> specList, List<GbGoodsSkuInfo> skuList){

        this.id = info.getGoodsId();
        this.name = info.getGoodsName();
        this.img = info.getGoodsImg();
        this.price = info.getSalesPrice();
        this.price2 = info.getMarketPrice();
        this.type = info.getGoodsType();
        this.stock = info.getIsStock();
        this.num = info.getGoodsNum();
        this.unit = info.getGoodsUnit();

        // 限购和数量
        this.limit = info.getIsLimit();
        this.quantity = info.getLimitNum();

        this.packs = GroupPackageResponse.getPackageResponseList(packList);
        this.specs = GroupSpecResponse.getSpecResponseList(specList);
        this.skus = GroupSkuResponse.getSkuResponseList(skuList);
    }
}
