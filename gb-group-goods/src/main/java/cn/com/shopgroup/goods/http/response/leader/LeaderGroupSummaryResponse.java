package cn.com.shopgroup.goods.http.response.leader;

import lombok.Data;

@Data
public class LeaderGroupSummaryResponse {
    // 实际收入（元）
    private Double totalFee;
    // 退款金额（元）
    private Double refundFee;
    // 跟团人数
    private Integer orderNum;
}
