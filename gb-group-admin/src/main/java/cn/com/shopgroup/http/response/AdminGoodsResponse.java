package cn.com.shopgroup.http.response;

import cn.com.shopgroup.goods.http.response.leader.LeaderSpecResponse;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台管理-商品详情/列表响应
 *
 * 字段与实体 GbGoodsInfo 保持一致(便于后台直接展示),
 * 额外补充规格列表(specList, 含规格值)
 */
@Data
public class AdminGoodsResponse {

    // 商品id,主键自增
    private Long goodsId;
    // 分类id,外键
    private Long catId;
    // 团长id,外键
    private Long leaderId;
    // 商品类型,1普通商品2称重商品
    private Byte goodsType;
    // 商品名称
    private String goodsName;
    // 商品主图
    private String goodsImg;
    // 进货价格
    private Double costPrice;
    // 销售价格
    private Double salesPrice;
    // 市场价格
    private Double marketPrice;

    // 是否启用库存
    private Byte isStock;
    // 商品库存
    private Integer goodsNum;

    // 是否启用限购
    private Byte isLimit;
    // 限购数量
    private Integer limitNum;

    // 商品单位
    private String goodsUnit;
    // 商品介绍
    private String goodsInfo;
    // 是否禁用 0 开启 1关闭
    private Byte isClose;
    // 平台审核
    private Byte isCheck;
    // 审核备注
    private String checkRemark;
    // 添加时间
    private Integer addTime;

    // 商品规格列表(包含规格值, 不含禁用规格/规格值); 调用方需按 goodsId 填充
    private List<LeaderSpecResponse> specList = new ArrayList<>();

    public AdminGoodsResponse() {

    }

    public AdminGoodsResponse(GbGoodsInfo info) {

        if (info == null) {
            return;
        }
        this.goodsId = info.getGoodsId();
        this.catId = info.getCatId();
        this.leaderId = info.getLeaderId();
        this.goodsType = info.getGoodsType();
        this.goodsName = info.getGoodsName();
        this.goodsImg = info.getGoodsImg();
        this.costPrice = info.getCostPrice();
        this.salesPrice = info.getSalesPrice();
        this.marketPrice = info.getMarketPrice();
        this.isStock = info.getIsStock();
        this.goodsNum = info.getGoodsNum();
        this.isLimit = info.getIsLimit();
        this.limitNum = info.getLimitNum();
        this.goodsUnit = info.getGoodsUnit();
        this.goodsInfo = info.getGoodsInfo();
        this.isClose = info.getIsClose();
        this.isCheck = info.getIsCheck();
        this.checkRemark = info.getCheckRemark();
        this.addTime = info.getAddTime();
    }

    // 列表转换
    public static List<AdminGoodsResponse> getAdminGoodsResponseList(List<GbGoodsInfo> lists) {

        List<AdminGoodsResponse> data = new ArrayList<>();
        if (lists == null) {
            return data;
        }
        for (GbGoodsInfo item : lists) {
            data.add(new AdminGoodsResponse(item));
        }
        return data;
    }
}