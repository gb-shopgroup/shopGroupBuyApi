package cn.com.shopgroup.goods.http.response.leader;

import cn.com.shopgroup.goods.model.GbGoodsPackageInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class leaderPackageResponse {

    private Long id;
    private String name;
    private Double price;

    public leaderPackageResponse(){

    }

    public leaderPackageResponse(GbGoodsPackageInfo data){

        this.id = data.getPackId();
        this.name = data.getPackName();
        this.price = data.getSalesPrice();
    }

    // 列表转换
    public static List<leaderPackageResponse> getPackageResponseList(List<GbGoodsPackageInfo> lists){

        List<leaderPackageResponse> data = new ArrayList<>();
        for(GbGoodsPackageInfo item : lists){
            data.add(new leaderPackageResponse(item));
        }
        return data;
    }

}
