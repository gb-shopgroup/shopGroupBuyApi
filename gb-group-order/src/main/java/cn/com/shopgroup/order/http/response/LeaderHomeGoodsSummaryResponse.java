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

    @ApiModelProperty(value = "订单商品总件数(共有X件商品)")
    private Long goodsTotal;
    @ApiModelProperty(value = "待核销总件数(商品数量-已核销数量-退款数量, 已退款部分不计入, 支持部分核销)")
    private Long unVerifyTotal;
    @ApiModelProperty(value = "当前页码")
    private Integer page;
    @ApiModelProperty(value = "每页数量")
    private Integer pageSize;
    @ApiModelProperty(value = "商品统计列表")
    private List<SummaryOrderGoodsResponse> list;

}
