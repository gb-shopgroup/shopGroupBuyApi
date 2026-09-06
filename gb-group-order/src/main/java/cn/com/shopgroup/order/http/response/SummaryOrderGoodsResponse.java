package cn.com.shopgroup.order.http.response;

import lombok.Data;

@Data
public class SummaryOrderGoodsResponse {

    // 商品ID
    private Long id;
    // 商品名称
    private String name;
    // 总数量
    private Long total;
    // 已核销数量
    private Long num1;
    // 未核销数量
    private Long num2;
    // 商品单位（如 斤/份/件）
    private String unit;

}
