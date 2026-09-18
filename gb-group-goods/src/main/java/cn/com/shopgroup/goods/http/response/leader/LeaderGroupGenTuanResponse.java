package cn.com.shopgroup.goods.http.response.leader;

import lombok.Data;

//团购活动详情统计
@Data
public class LeaderGroupGenTuanResponse {
    // 成员
    private Integer memberNum;
    // 跟团人次（订单数）
    private Integer orderNum;
    // 订单总金额（不管退的，支付总金额）
    private Double totalAmount;
}
