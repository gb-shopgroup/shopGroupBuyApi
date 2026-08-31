package cn.com.shopgroup.common.yeepay;

import lombok.Data;

@Data
public class YeepayDivideOrderItem {


    private String ledgerNo = "";


    private String amount = "";


    private String divideDetailDesc = "";


    private String ledgerType = "MERCHANT2MERCHANT";

    public YeepayDivideOrderItem(){

    }

    public YeepayDivideOrderItem(String no, double fee, String desc){

        this.ledgerNo = no;
        this.amount = String.valueOf(fee);
        this.divideDetailDesc = desc;
        this.ledgerType = "MERCHANT2MERCHANT";
    }
}
