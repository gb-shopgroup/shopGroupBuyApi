package cn.com.shopgroup.goods.http.response.group;

import cn.com.shopgroup.goods.model.GbGoodsInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupActGoodsResponse {

    private Long gid;
    private String gname;
    private Byte gtype;
    private String img;
    private Double price;
    private Double price2;
    private String stock;

    public GroupActGoodsResponse() {

    }

    public GroupActGoodsResponse(GbGoodsInfo info) {

        this.gid = info.getGoodsId();
        this.gname = info.getGoodsName();
        this.gtype = info.getGoodsType();
        this.img = info.getGoodsImg();
        this.price = info.getSalesPrice();
        this.price2 = info.getMarketPrice();
        this.stock = info.getGoodsNum() + info.getGoodsUnit();
    }

    // 列表转换
    public static List<GroupActGoodsResponse> getGroupActGoodsResponseList(List<GbGoodsInfo> lists) {

        List<GroupActGoodsResponse> data = new ArrayList<>();
        for (GbGoodsInfo item : lists) {
            data.add(new GroupActGoodsResponse(item));
        }
        return data;
    }

}
