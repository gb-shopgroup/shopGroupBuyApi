package cn.com.shopgroup.goods.http.response.leader;

import cn.com.shopgroup.goods.model.GbGoodsInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LeaderGroupGoodsResponse {

    private Long gid;
    private String gname;
    private Byte gtype;
    private String img;
    private Double price;
    private Double price2;
    private String stock;

    public LeaderGroupGoodsResponse(){

    }

    public LeaderGroupGoodsResponse(GbGoodsInfo info){

        this.gid = info.getGoodsId();
        this.gname = info.getGoodsName();
        this.gtype = info.getGoodsType();
        this.img = info.getGoodsImg();
        this.price = info.getSalesPrice();
        this.price2 = info.getMarketPrice();
        this.stock = info.getGoodsNum() + info.getGoodsUnit();
    }

    // 列表转换
    public static List<LeaderGroupGoodsResponse> getGroupGoodsResponseList(List<GbGoodsInfo> lists){

        List<LeaderGroupGoodsResponse> data = new ArrayList<>();
        for(GbGoodsInfo item : lists){
            data.add(new LeaderGroupGoodsResponse(item));
        }
        return data;
    }

}
