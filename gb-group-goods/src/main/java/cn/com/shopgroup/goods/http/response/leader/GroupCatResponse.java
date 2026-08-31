package cn.com.shopgroup.goods.http.response.leader;

import cn.com.shopgroup.goods.model.GbGroupCategoryInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupCatResponse {

    private Long id;
    private String name;

    public GroupCatResponse() {

    }

    public GroupCatResponse(GbGroupCategoryInfo data) {

        this.id = data.getCatId();
        this.name = data.getCatName();
    }

    // 列表转换
    public static List<GroupCatResponse> getGroupCatResponseList(List<GbGroupCategoryInfo> lists) {

        List<GroupCatResponse> data = new ArrayList<>();
        for (GbGroupCategoryInfo item : lists) {
            data.add(new GroupCatResponse(item));
        }
        return data;
    }
}
