package cn.com.shopgroup.goods.http.response.group;

import cn.com.shopgroup.goods.model.GbGroupActivityInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupActivityResponse {

    private Long id;
    private String name;
    private String img;
    private String img2;
    private String img3;
    private Double price;
    private Double price2;
    private Double price3;
    private String brief;

    private Long lid;        // 团长id
    private Byte pickup;     // 商品提货方式：1自提2邮递
    private Integer num;       // 订单数量
    private Integer num2;      // 虚拟数量

    private Byte isClose;   // 是否关闭


    public GroupActivityResponse(){

    }

    public GroupActivityResponse(GbGroupActivityInfo data){

        this.id = data.getGroupId();
        this.name = data.getGroupName();
        this.img = data.getGroupImg();
        this.img2 = data.getGroupImg2();
        this.img3 = data.getGroupImg3();
        this.price = data.getGroupPrice();
        this.price2 = data.getGroupPrice2();
        this.price3 = data.getMarketPrice();
        this.brief = data.getGroupInfo();

        this.lid = data.getLeaderId();      // 团长id
        this.pickup = data.getPickupStyle();// 自提/邮递
        this.num = data.getOrderTotal();    // 订单数量
        this.num2 = data.getVirtualOrder(); // 虚拟数量

        this.isClose = data.getIsClose();   // 是否关闭
    }

    // 列表转换
    public static List<GroupActivityResponse> getGroupActivityResponseList(List<GbGroupActivityInfo> lists){

        List<GroupActivityResponse> data = new ArrayList<>();
        for(GbGroupActivityInfo item : lists){
            data.add(new GroupActivityResponse(item));
        }
        return data;
    }
}
