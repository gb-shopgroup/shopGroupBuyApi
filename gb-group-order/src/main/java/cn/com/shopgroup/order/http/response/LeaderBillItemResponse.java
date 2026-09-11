package cn.com.shopgroup.order.http.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 团长端-对账单明细项(按商品维度输出商品信息, 按订单维度输出订单号)
 */
@Data
@ApiModel("对账单明细项")
public class LeaderBillItemResponse {

    //商品id(按商品统计时返回)
    @ApiModelProperty("商品id(按商品统计时返回)")
    private Long goodsId;

    //商品名称(按商品统计时返回)
    @ApiModelProperty("商品名称(按商品统计时返回)")
    private String goodsName;

    //订单号(按订单统计时返回)
    @ApiModelProperty("订单号(按订单统计时返回)")
    private String orderNo;

    //订单金额，单位元(按订单统计=实付金额; 按商品统计=Σ商品单价×购买数量)
    @ApiModelProperty("订单金额，单位元")
    private Double amount;

    //退款金额，单位元(按订单统计=退款成功累计; 按商品统计=Σ商品单价×退款数量, 含待审核不含被拒)
    @ApiModelProperty("退款金额，单位元")
    private Double refundAmount;
}
