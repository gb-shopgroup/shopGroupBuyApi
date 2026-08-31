package cn.com.shopgroup.goods.http.response.group;

import cn.com.shopgroup.goods.model.GbGoodsSpecValue;
import lombok.Data;

@Data
public class GroupSpecValResponse {

    private Long valId;
    private String valName;

    public GroupSpecValResponse(){

    }

    public GroupSpecValResponse(GbGoodsSpecValue data){

        this.valId = data.getValId();
        this.valName = data.getSpecVal();
    }

}
