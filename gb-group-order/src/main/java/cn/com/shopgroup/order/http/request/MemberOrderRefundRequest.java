package cn.com.shopgroup.order.http.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

//用户端申请退货请求参数
@Data
public class MemberOrderRefundRequest {

    //订单号
    @NotNull(message = "订单号不能为空")
    private String orderNo;
    //退款类型标识 1 退款 2 退货退款
    @NotNull(message = "退款类型不能为空")
    private Integer refundFlag;
}
