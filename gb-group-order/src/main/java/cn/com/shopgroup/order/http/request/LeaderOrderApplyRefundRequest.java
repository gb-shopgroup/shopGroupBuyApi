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

    // 开始日期 yyyy-MM-dd(按订单下单时间过滤, 取当天00:00:00), 未传则不限制开始时间
    private String startDate;

    // 结束日期 yyyy-MM-dd(按订单下单时间过滤, 取当天23:59:59), 未传则不限制结束时间
    private String endDate;

    private Integer page;

    private Integer pageSize;
}
