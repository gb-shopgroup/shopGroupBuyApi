package cn.com.shopgroup.goods.http.response.group;

import cn.com.shopgroup.goods.model.GbGoodsPackageInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupPackageResponse {

    private Long id;
    private String name;
    private Double price;

    public GroupPackageResponse(){

    }

    public GroupPackageResponse(GbGoodsPackageInfo data){

        this.id = data.getPackId();
        this.name = data.getPackName();
        this.price = data.getSalesPrice();
    }

    // 列表转换
    public static List<GroupPackageResponse> getPackageResponseList(List<GbGoodsPackageInfo> lists){

        List<GroupPackageResponse> data = new ArrayList<>();
        for(GbGoodsPackageInfo item : lists){
            data.add(new GroupPackageResponse(item));
        }
        return data;
    }

}
