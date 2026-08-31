package cn.com.shopgroup.goods.http.response;

import cn.com.shopgroup.goods.model.GbGoodsCategoryInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GoodsCategoryResponse {

    private Long id;
    private String name;

    public GoodsCategoryResponse() {

    }

    public GoodsCategoryResponse(GbGoodsCategoryInfo data) {

        this.id = data.getCatId();
        this.name = data.getCatName();
    }

    // 列表转换
    public static List<GoodsCategoryResponse> getGoodsCategoryResponseList(List<GbGoodsCategoryInfo> lists) {

        List<GoodsCategoryResponse> data = new ArrayList<>();
        for (GbGoodsCategoryInfo item : lists) {
            data.add(new GoodsCategoryResponse(item));
        }
        return data;
    }
}
