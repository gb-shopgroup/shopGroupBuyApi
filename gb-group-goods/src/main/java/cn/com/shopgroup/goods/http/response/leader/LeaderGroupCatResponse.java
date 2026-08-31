package cn.com.shopgroup.goods.http.response.leader;

import cn.com.shopgroup.goods.model.GbGroupCategoryInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LeaderGroupCatResponse {

    private Long id;
    private String name;

    public LeaderGroupCatResponse(){

    }

    public LeaderGroupCatResponse(GbGroupCategoryInfo data){

        this.id = data.getCatId();
        this.name = data.getCatName();
    }

    // 列表转换
    public static List<LeaderGroupCatResponse> getGroupCatResponseList(List<GbGroupCategoryInfo> lists){

        List<LeaderGroupCatResponse> data = new ArrayList<>();
        for(GbGroupCategoryInfo item : lists){
            data.add(new LeaderGroupCatResponse(item));
        }
        return data;
    }
}
