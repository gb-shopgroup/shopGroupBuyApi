package cn.com.shopgroup.goods.http.response.group;

import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGroupActivityGoods;
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
        this.stock = (info.getGoodsNum() == null ? "" : info.getGoodsNum()) + (info.getGoodsUnit() == null ? "" : info.getGoodsUnit());
    }

    /**
     * 团购商品构造: 价格取团购商品表冗余的团购价, 库存/单位取自商品表
     */
    public GroupActGoodsResponse(GbGroupActivityGoods goods, GbGoodsInfo goodsInfo) {

        this.gid = goods.getGoodsId();
        this.gname = goods.getGoodsName();
        this.gtype = goods.getGoodsType();
        this.img = goods.getGroupImg();
        // 展示团购价/团购市场价
        this.price = goods.getGroupPrice();
        this.price2 = goods.getMarketPrice();
        // 库存/单位补充(商品表)
        Integer num = goodsInfo == null ? null : goodsInfo.getGoodsNum();
        String unit = goodsInfo == null ? null : goodsInfo.getGoodsUnit();
        this.stock = (num == null ? "" : num) + (unit == null ? "" : unit);
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
