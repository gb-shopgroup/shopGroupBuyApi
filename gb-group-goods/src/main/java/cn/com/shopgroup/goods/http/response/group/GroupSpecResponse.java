package cn.com.shopgroup.goods.http.response.group;

import cn.com.shopgroup.goods.model.GbGoodsSpecInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecValue;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupSpecResponse {

    private Long specId;
    private String specName;

    private List<GroupSpecValResponse> valList;

    public GroupSpecResponse(){

    }

    public GroupSpecResponse(GbGoodsSpecInfo data){

        this.specId = data.getSpecId();
        this.specName = data.getSpecName();

        if(data.getSpecValueList() != null){
            List<GroupSpecValResponse> tempList = new ArrayList<>();
            for(GbGoodsSpecValue item : data.getSpecValueList()){
                GroupSpecValResponse temp = new GroupSpecValResponse(item);
                tempList.add(temp);
            }
            this.valList = tempList;
        }else{
            this.valList = new ArrayList<>();
        }
    }

    // 列表转换
    public static List<GroupSpecResponse> getSpecResponseList(List<GbGoodsSpecInfo> lists){

        List<GroupSpecResponse> data = new ArrayList<>();
        for(GbGoodsSpecInfo item : lists){
            data.add(new GroupSpecResponse(item));
        }
        return data;
    }


}
