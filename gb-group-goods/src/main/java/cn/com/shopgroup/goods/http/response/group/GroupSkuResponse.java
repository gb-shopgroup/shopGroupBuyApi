package cn.com.shopgroup.goods.http.response.group;

import cn.com.shopgroup.goods.model.GbGoodsSkuInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupSkuResponse {

    private Long id;
    private String ids;
    private Integer stock;
    private Double price;
    private Double price2;

    public GroupSkuResponse(){

    }

    public GroupSkuResponse(GbGoodsSkuInfo data){

        this.id = data.getSkuId();
        this.ids = data.getSkuIds();
        this.stock = data.getGoodsNum();
        this.price = data.getSalesPrice();
        this.price2 = data.getMarketPrice();
    }

    // 列表转换
    public static List<GroupSkuResponse> getSkuResponseList(List<GbGoodsSkuInfo> lists){

        List<GroupSkuResponse> data = new ArrayList<>();
        for(GbGoodsSkuInfo item : lists){
            data.add(new GroupSkuResponse(item));
        }
        return data;
    }

}
