package cn.com.shopgroup.order.http.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OrderRequest {

    @NotNull(message = "团购不能为空")
    private Long groupId;

    @NotNull(message = "提货点不能为空")
    private Long pointId;

    @NotNull(message = "姓名不能为空")
    private String name;

    @NotNull(message = "电话不能为空")
    private String mobile;

    // 邮寄地址
    //private int addressId;

    // 客户订单备注
    //private String remark;

    @NotEmpty(message = "订单商品不能为空")
    private List<OrderGoodsRequest> goods;

}
