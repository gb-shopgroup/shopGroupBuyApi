package cn.com.shopgroup.order.http.response;

import lombok.Data;

@Data
public class SummaryOrderResponse {

    private Long total; // 总数量
    private Long num1; // 已核销数量
    private Long num2; // 未核销数量

}
