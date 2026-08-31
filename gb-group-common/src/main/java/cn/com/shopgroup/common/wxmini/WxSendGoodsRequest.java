package cn.com.shopgroup.common.wxmini;

import cn.com.shopgroup.common.utils.TimeUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WxSendGoodsRequest {


    private WxSendGoodsOrderKey order_key;

    private int delivery_mode;

    private int logistics_type;

    private List<WxSendGoodsShippingList> shipping_list;

    private String upload_time;

    private WxSendGoodsPayer payer;

    public WxSendGoodsRequest(){

    }

    public WxSendGoodsRequest(String transactionId, String goodsName, String openid){

        this.order_key = new WxSendGoodsOrderKey(2, transactionId);
        this.delivery_mode = 1;
        this.logistics_type = 4;
        this.shipping_list = new ArrayList<>();
        this.shipping_list.add(new WxSendGoodsShippingList(goodsName));
        this.upload_time = TimeUtils.getNowTimeStr2();
        this.payer = new WxSendGoodsPayer(openid);
    }
}