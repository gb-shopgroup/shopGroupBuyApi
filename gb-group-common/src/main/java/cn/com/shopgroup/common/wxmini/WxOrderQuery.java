package cn.com.shopgroup.common.wxmini;

import lombok.Data;

@Data
public class WxOrderQuery {

    private String transaction_id;

    public WxOrderQuery(){

    }

    public WxOrderQuery(String id){

        this.transaction_id = id;
    }

}
