package cn.com.shopgroup.common.wxmini;

import lombok.Data;

@Data
public class WxSendGoodsShippingList {


    String item_desc;

    public WxSendGoodsShippingList(){}

    public WxSendGoodsShippingList(String item){

  
        this.item_desc = item.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "");
    }

}