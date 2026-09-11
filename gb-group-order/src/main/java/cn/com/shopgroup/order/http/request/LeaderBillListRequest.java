package cn.com.shopgroup.order.http.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 团长端-对账单查询请求
 */
@Data
@ApiModel("对账单查询请求")
public class LeaderBillListRequest {

    //统计维度: 1=按商品(明细返回商品名称), 2=按订单(明细返回订单号), 不传默认1
    @ApiModelProperty("统计维度: 1=按商品, 2=按订单, 默认1")
    private Integer type;

    //开始日期 yyyy-MM-dd, 与结束日期成对传入, 未输入时间则不查询
    @ApiModelProperty("开始日期 yyyy-MM-dd HH:mm:ss(与结束日期成对传入, 未传入则不查询)")
    private String startDate;

    //结束日期 yyyy-MM-dd, 与开始日期成对传入, 未输入时间则不查询
    @ApiModelProperty("结束日期 yyyy-MM-dd HH:mm:ss(与开始日期成对传入, 未传入则不查询)")
    private String endDate;

    //页码，默认1
    @ApiModelProperty("页码，默认1")
    private Integer page;

    //每页数量，默认10，最大20
    @ApiModelProperty("每页数量，默认10，最大20")
    private Integer pageSize;
}
