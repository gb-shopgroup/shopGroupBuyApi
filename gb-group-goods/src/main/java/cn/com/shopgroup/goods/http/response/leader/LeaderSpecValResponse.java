package cn.com.shopgroup.goods.http.response.leader;

import cn.com.shopgroup.goods.model.GbGoodsSpecValue;
import lombok.Data;

@Data
public class LeaderSpecValResponse {

    private Long valId;
    private String valName;

    public LeaderSpecValResponse(){

    }

    public LeaderSpecValResponse(GbGoodsSpecValue data){

        this.valId = data.getValId();
        this.valName = data.getSpecVal();
    }

}
