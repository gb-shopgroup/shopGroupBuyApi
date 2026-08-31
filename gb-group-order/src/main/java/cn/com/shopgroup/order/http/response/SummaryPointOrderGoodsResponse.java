package cn.com.shopgroup.order.http.response;

import lombok.Data;

import java.util.List;

@Data
public class SummaryPointOrderGoodsResponse {

    private Long id;
    private String name;
    private List<SummaryOrderGoodsResponse> lists;

    public SummaryPointOrderGoodsResponse(){

    }

    public SummaryPointOrderGoodsResponse(long pid, String pname){

        this.id = pid;
        this.name = pname;
    }
}
