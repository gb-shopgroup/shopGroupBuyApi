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
    // 已核销数量(商品行核销数量汇总)
    private Long num1;
    // 待核销数量(商品数量-已核销数量-退款数量, 已退款部分不计入)
    private Long num2;
    // 商品单位（如 斤/份/件）
    private String unit;
    // 商品规格(订单商品行 sku_names 去重拼接, 如 "500g,1斤装"; 无规格商品为 null)
    private String skuNames;

}
