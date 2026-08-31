package cn.com.shopgroup.common.wxmini;

import lombok.Data;

@Data
public class WxSendGoodsPayer {

    private String openid;

    public WxSendGoodsPayer(){
    }

    public WxSendGoodsPayer(String oid) {
        this.openid = oid;
    }

}