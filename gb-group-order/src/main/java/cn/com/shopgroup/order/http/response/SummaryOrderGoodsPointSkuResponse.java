package cn.com.shopgroup.order.http.response;

import lombok.Data;

import java.util.List;

@Data
public class SummaryOrderGoodsPointSkuResponse {

    private Long pid;        // 提货点
    private String pname;   // 提货点名称

    // 提货点下不同规格的数量汇总
    private List<SummaryOrderGoodsSkuResponse> lists;

    public SummaryOrderGoodsPointSkuResponse(){

    }

    public SummaryOrderGoodsPointSkuResponse(Long id, String name){

        this.pid = id;
        this.pname = name;
    }

}
