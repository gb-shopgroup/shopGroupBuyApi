package cn.com.shopgroup.common.merchant;

import lombok.Data;

@Data
public class MerchantInfo {

    private Long busId;
    private String merchantNo;
    private Integer money;

    public MerchantInfo(){

    }

    public MerchantInfo(Long id, String no, Integer money){

        this.busId = id;
        this.merchantNo = no;
        this.money = money;
    }
}
