package cn.com.shopgroup.order.http.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 团长端首页商品统计
 */
@Data
@ApiModel(value = "团长端首页商品统计对象")
public class LeaderHomeGoodsSummaryResponse {

    @ApiModelProperty(value = "商品种类总数(共有X件商品)")
    private Long goodsTotal;
    @ApiModelProperty(value = "待核销总件数")
    private Long unVerifyTotal;
    @ApiModelProperty(value = "当前页码")
    private Integer page;
    @ApiModelProperty(value = "每页数量")
    private Integer pageSize;
    @ApiModelProperty(value = "商品统计列表")
    private List<SummaryOrderGoodsResponse> list;

}
