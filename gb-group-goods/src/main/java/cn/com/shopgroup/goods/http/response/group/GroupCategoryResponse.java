package cn.com.shopgroup.goods.http.response.group;

import cn.com.shopgroup.goods.model.GbGroupCategoryInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupCategoryResponse {

    private Long id;
    private String name;

    public GroupCategoryResponse(){

    }

    public GroupCategoryResponse(GbGroupCategoryInfo data){

        this.id = data.getCatId();
        this.name = data.getCatName();
    }

    // 列表转换
    public static List<GroupCategoryResponse> getGroupCategoryResponseList(List<GbGroupCategoryInfo> lists){

        List<GroupCategoryResponse> data = new ArrayList<>();
        for(GbGroupCategoryInfo item : lists){
            data.add(new GroupCategoryResponse(item));
        }
        return data;
    }
}
