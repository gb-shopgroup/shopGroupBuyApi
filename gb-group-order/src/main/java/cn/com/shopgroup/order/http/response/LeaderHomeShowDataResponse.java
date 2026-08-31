package cn.com.shopgroup.order.http.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 团长端首页展示订单数据统计
 */
@Data
@ApiModel(value = "团长端首页展示订单数据统计对象")
public class LeaderHomeShowDataResponse {

    @ApiModelProperty(value = "有效订单总数")
    private Integer orderTotal;
    @ApiModelProperty(value = "订单总金额")
    private Double amountTotal;
    @ApiModelProperty(value = "退款总金额")
    private Double refundAmountTotal;

}
