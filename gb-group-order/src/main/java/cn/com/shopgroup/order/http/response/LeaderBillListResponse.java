package cn.com.shopgroup.order.http.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 团长端-对账单(顶部统计 + 维度明细)
 */
@Data
@ApiModel("团长端对账单")
public class LeaderBillListResponse {

    //统计维度: 1=按商品, 2=按订单
    @ApiModelProperty("统计维度: 1=按商品, 2=按订单")
    private Integer type;

    //有效订单数(已支付且未取消, 按下单时间归集)
    @ApiModelProperty("有效订单数")
    private Integer orderTotal;

    //订单总金额，单位元
    @ApiModelProperty("订单总金额，单位元")
    private Double amountTotal;

    //退款总金额，单位元
    @ApiModelProperty("退款总金额，单位元")
    private Double refundAmountTotal;

    //明细总条数(分页用: 按商品=商品种类数, 按订单=有效订单数)
    @ApiModelProperty("明细总条数")
    private Integer total;

    //页码
    @ApiModelProperty("页码")
    private Integer page;

    //每页数量
    @ApiModelProperty("每页数量")
    private Integer pageSize;

    //对账明细列表: 按商品维度返回商品名称/订单金额/退款金额, 按订单维度返回订单号/订单金额/退款金额
    @ApiModelProperty("对账明细列表")
    private List<LeaderBillItemResponse> list = new ArrayList<>();
}
