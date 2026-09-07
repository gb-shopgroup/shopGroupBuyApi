package cn.com.shopgroup.yeepay;

import lombok.Data;

@Data
public class YeePayDivideOrderItem {


    private String ledgerNo = "";


    private String amount = "";


    private String divideDetailDesc = "";


    private String ledgerType = "MERCHANT2MERCHANT";

    public YeePayDivideOrderItem(){

    }

    public YeePayDivideOrderItem(String no, double fee, String desc){

        this.ledgerNo = no;
        this.amount = String.valueOf(fee);
        this.divideDetailDesc = desc;
        this.ledgerType = "MERCHANT2MERCHANT";
    }
}
