package cn.com.shopgroup.order.http.response;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.order.model.GbOrderVerifyRecord;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 订单核销记录响应(订单详情返回, 支持一单多次部分核销产生多条记录)
@Data
public class OrderVerifyRecordResponse {

    // 核销记录id
    private Long id;
    // 核销类型: 0=团长后台核销, 2=用户扫码核销
    private Integer verifyType;
    // 核销人id: 团长后台核销=团长/店员id, 用户扫码核销=用户memberId
    private Long staffId;
    // 核销人姓名: 团长后台核销=团长/店员姓名, 用户扫码核销=用户昵称
    private String staffName;
    // 核销(实际领取)自提点id
    private Long verifyPointId;
    // 核销(实际领取)自提点名称
    private String verifyPointName;
    // 核销时间(格式化字符串)
    private String addTime;
    // 核销商品明细(仅本次核销数量>0的商品行):
    // 每行 goodsId/goodsName/skuNames/goodsPrice(元)/goodsUnit/goodsNum(购买数)/verifyNum(本次核销数)/receiptNum(累计已核销数)
    private List<Map<String, Object>> verifyGoodsMsg;

    public OrderVerifyRecordResponse() {

    }

    public OrderVerifyRecordResponse(GbOrderVerifyRecord data) {
        if (data == null) {
            return;
        }
        this.id = data.getId();
        this.verifyType = data.getVerifyType();
        this.staffId = data.getStaffId();
        this.staffName = data.getStaffName();
        this.verifyPointId = data.getVerifyPointId();
        this.verifyPointName = data.getVerifyPointName();
        this.addTime = TimeUtils.getFormatTimeStamp(data.getAddTime());
        // 核销商品明细: 库表存的是JSON数组字符串, 解析为对象数组返回
        if (StringUtils.isEmpty(data.getVerifyGoodsMsg())) {
            this.verifyGoodsMsg = new ArrayList<>();
        } else {
            this.verifyGoodsMsg = JSON.parseObject(data.getVerifyGoodsMsg(), new TypeReference<List<Map<String, Object>>>() {
            });
        }
    }

    // 列表转换(按核销时间正序)
    public static List<OrderVerifyRecordResponse> getOrderVerifyRecordResponseList(List<GbOrderVerifyRecord> records) {

        List<OrderVerifyRecordResponse> data = new ArrayList<>();
        if (records == null) {
            return data;
        }
        for (GbOrderVerifyRecord item : records) {
            data.add(new OrderVerifyRecordResponse(item));
        }
        return data;
    }
}
