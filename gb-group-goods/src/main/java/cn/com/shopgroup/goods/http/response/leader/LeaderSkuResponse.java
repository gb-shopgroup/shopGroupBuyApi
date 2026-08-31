package cn.com.shopgroup.goods.http.response.leader;

import cn.com.shopgroup.goods.model.GbGoodsSkuInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LeaderSkuResponse {

    // skuid
    private Long id;
    // 商品id
    private Long gid;

    // sku id, 规格1+规格2+......
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
    // 是否禁用
    private Byte isClose;

    public LeaderSkuResponse(){

    }

    public LeaderSkuResponse(GbGoodsSkuInfo data){

        this.id = data.getSkuId();
        this.gid = data.getGoodsId();
        this.ids = data.getSkuIds();
        this.names = data.getSkuNames();
        this.price = data.getSalesPrice();
        this.price2 = data.getMarketPrice();
        this.num = data.getGoodsNum();
        this.img = data.getGoodsImg();
        this.isClose = data.getIsClose();
    }

    // 列表转换
    public static List<LeaderSkuResponse> getSkuResponseList(List<GbGoodsSkuInfo> lists){

        List<LeaderSkuResponse> data = new ArrayList<>();
        for(GbGoodsSkuInfo item : lists){
            data.add(new LeaderSkuResponse(item));
        }
        return data;
    }
}
