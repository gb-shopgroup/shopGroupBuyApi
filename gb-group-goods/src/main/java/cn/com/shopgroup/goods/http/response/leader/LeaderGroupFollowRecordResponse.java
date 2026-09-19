package cn.com.shopgroup.goods.http.response.leader;

import lombok.Data;

// 团购活动详情-跟团记录(真实订单数据, 按购买时间倒序)
@Data
public class LeaderGroupFollowRecordResponse {

    // 下单用户手机号
    private String mobile;
    // 下单用户姓名(昵称)
    private String name;
    // 下单用户头像
    private String avatar;
    // 购买时间(格式化 yyyy-MM-dd HH:mm:ss, 取支付时间)
    private String buyTime;
    // 购买商品描述(多件商品按 商品名/规格 拼接)
    private String goodsDesc;
    // 购买数量(订单商品数量合计)
    private Integer buyNum;
}
