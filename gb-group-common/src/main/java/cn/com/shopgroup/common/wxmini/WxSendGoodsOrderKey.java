package cn.com.shopgroup.common.wxmini;

import lombok.Data;

@Data
public class WxSendGoodsOrderKey {


    private int order_number_type;


    private String transaction_id;

    public WxSendGoodsOrderKey(){
    }

    public WxSendGoodsOrderKey(int type, String tid){
        this.order_number_type = type;
        this.transaction_id = tid;
    }
}