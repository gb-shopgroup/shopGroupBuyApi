package cn.com.shopgroup.goods.http.response.leader;

import cn.com.shopgroup.goods.http.response.group.GroupSpecResponse;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LeaderGoodsResponse {

    // 商品id,主键自增
    private Long id;
    // 分类id,外键
    private Long cat;
    // 商品类型,1普通商品2称重商品
    private Byte type;
    // 商品名称
    private String name;
    // 商品主图
    private String img;
    private String img2;
    private String img3;
    // 销售价格
    private Double price;
    // 市场价格
    private Double price2;

    // 是否设置库存
    private Byte isStock;
    // 商品库存,总库存
    private Integer num;

    // 是否设置限购
    private Byte isLimit;
    // 限购数量
    private Integer num2;

    // 商品单位
    private String unit;
    // 是否禁用
    private Byte isClose;
    // 平台审核
    private Byte isCheck;
    // 审核备注
    private String checkRemark;

    // 商品规格列表(包含规格值)
    private List<LeaderSpecResponse> specList = new ArrayList<>();

    public LeaderGoodsResponse(){

    }

    public LeaderGoodsResponse(GbGoodsInfo info){

        // 商品id,主键自增
        this.id = info.getGoodsId();
        // 分类id,外键
        this.cat = info.getCatId();
        // 商品类型,1普通商品2称重商品
        this.type = info.getGoodsType();
        // 商品名称
        this.name = info.getGoodsName();
        // 商品主图
        this.img = info.getGoodsImg();
        // 销售价格
        this.price = info.getSalesPrice();
        // 市场价格
        this.price2 = info.getMarketPrice();

        // 是否设置库存
        this.isStock = info.getIsStock();
        // 商品库存,总库存
        this.num = info.getGoodsNum();

        // 是否设置限购
        this.isLimit = info.getIsLimit();
        // 限购数量
        this.num2 = info.getLimitNum();

        // 商品单位
        this.unit = info.getGoodsUnit();
        // 是否禁用
        this.isClose = info.getIsClose();
        // 平台审核
        this.isCheck = info.getIsCheck();
        // 审核备注
        this.checkRemark = info.getCheckRemark();
    }

    // 列表转换
    public static List<LeaderGoodsResponse> getGoodsResponseList(List<GbGoodsInfo> lists){

        List<LeaderGoodsResponse> data = new ArrayList<>();
        for(GbGoodsInfo info : lists){
            data.add(new LeaderGoodsResponse(info));
        }
        return data;
    }
}
