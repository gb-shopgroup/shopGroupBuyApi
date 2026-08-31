package cn.com.shopgroup.goods.http.response.leader;

import cn.com.shopgroup.goods.model.GbGoodsSpecInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecValue;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LeaderSpecResponse {

    private Long specId;
    private String specName;

    private List<LeaderSpecValResponse> valList;

    public LeaderSpecResponse(){

    }

    public LeaderSpecResponse(GbGoodsSpecInfo data){

        this.specId = data.getSpecId();
        this.specName = data.getSpecName();

        if(data.getSpecValueList() != null){
            List<LeaderSpecValResponse> tempList = new ArrayList<>();
            for(GbGoodsSpecValue item : data.getSpecValueList()){
                LeaderSpecValResponse temp = new LeaderSpecValResponse(item);
                tempList.add(temp);
            }
            this.valList = tempList;
        }else{
            this.valList = new ArrayList<>();
        }
    }

    // 列表转换
    public static List<LeaderSpecResponse> getSpecResponseList(List<GbGoodsSpecInfo> lists){

        List<LeaderSpecResponse> data = new ArrayList<>();
        for(GbGoodsSpecInfo item : lists){
            data.add(new LeaderSpecResponse(item));
        }
        return data;
    }


}
