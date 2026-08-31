package cn.com.shopgroup.goods.http.response.leader;

import cn.com.shopgroup.goods.http.response.group.GroupActGoodsResponse;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupActResponse {

    // 团购id
    private Long id;
    // 团长id(分享使用)
    private Long lid;
    // 团购分类id
    private Long cat;
    // 团购名称
    private String name;
    // 商品提货方式,1自提2邮递
    private Byte pickup;
    // 团购价格/最小价格
    private Double price;
    // 团购价格/最大价格
    private Double price2;
    // 团购图片
    private String img;
    // 团购图片
    private String img2;
    // 团购图片
    private String img3;
    // 团购介绍
    private String info;
    // 虚拟订单数量
    private Integer virtual;
    // 实际订单数量
    private Integer order;
    // 是否禁用,0上线1下线
    private Byte isClose;
    // 开团时间
    private Integer startTime;
    // 结束时间
    private Integer endTime;

    // 平台审核
    private Byte isCheck;
    // 审核备注
    private String checkRemark;

    // 团购商品列表
    private List<GroupActGoodsResponse> goods;

    public GroupActResponse() {

    }

    public GroupActResponse(GbGroupActivityInfo data) {

        // 团购id
        this.id = data.getGroupId();
        // 团长id
        this.lid = data.getLeaderId();
        // 团购分类id
        this.cat = data.getCatId();
        // 团购名称
        this.name = data.getGroupName();
        // 商品提货方式,1自提2邮递
        this.pickup = data.getPickupStyle();
        // 团购价格
        this.price = data.getGroupPrice();
        // 团购价格2
        this.price2 = data.getGroupPrice2();
        // 团购图片
        this.img = data.getGroupImg();
        this.img2 = data.getGroupImg2();
        this.img3 = data.getGroupImg3();
        // 团购介绍
        this.info = data.getGroupInfo();
        // 虚拟订单数量
        this.virtual = data.getVirtualOrder();
        // 实际订单数量
        this.order = data.getOrderTotal();
        // 是否禁用,0上线1下线
        this.isClose = data.getIsClose();
        // 平台审核
        this.isCheck = data.getIsCheck();
        this.startTime = data.getStartTime();
        this.endTime = data.getEndTime();
        // 审核备注
        this.checkRemark = data.getCheckRemark();
    }

    // 列表转换
    public static List<GroupActResponse> getGroupResponseList(List<GbGroupActivityInfo> lists) {

        List<GroupActResponse> data = new ArrayList<>();
        for (GbGroupActivityInfo item : lists) {
            data.add(new GroupActResponse(item));
        }
        return data;
    }


}
