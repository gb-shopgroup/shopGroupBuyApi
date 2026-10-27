package cn.com.shopgroup.common.wxmini;

import lombok.Data;

@Data
public class WxSendGoodsShippingList {


    String item_desc;

    public WxSendGoodsShippingList(){}

    public WxSendGoodsShippingList(String item){

        // 过滤掉非中英文/数字的字符, 保留英文逗号(多个商品名称用逗号组装时的分隔符); 空值兜底为空字符串
        String desc = item == null ? "" : item.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5,]", "");
        // 过滤后可能残留多余逗号(如商品名被全部过滤), 去除首尾及连续逗号
        this.item_desc = desc.replaceAll(",{2,}", ",").replaceAll("^,|,$", "");
    }

}