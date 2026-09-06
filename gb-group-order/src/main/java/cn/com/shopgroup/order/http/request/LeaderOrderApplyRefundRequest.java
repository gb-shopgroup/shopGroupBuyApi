package cn.com.shopgroup.order.http.request;

import lombok.Data;

//团长端查询-售后-订单列表参数
@Data
public class LeaderOrderApplyRefundRequest {

    //商品名称 或是手机号
    private String keyword;
    //审核状态[不传 查询全部 1 待审核 2 同意 3 不同意]
    private Integer applyStatus;
    //团活动Id
    private Long groupId;
    //先不考虑
    private Long pointId;

    private Integer page;

    private Integer pageSize;
}
