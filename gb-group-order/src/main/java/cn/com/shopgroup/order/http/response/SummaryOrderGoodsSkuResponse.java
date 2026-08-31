package cn.com.shopgroup.order.http.response;

import lombok.Data;

@Data
public class SummaryOrderGoodsSkuResponse {

    private String id;      // skuids
    private String name;    // skunames
    private Long total;     // 总数量汇总

    public SummaryOrderGoodsSkuResponse(){

    }

    public SummaryOrderGoodsSkuResponse(String id, String name, long total){

        this.id = id;
        this.name = name;
        this.total = total;
    }

}
