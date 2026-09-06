package cn.com.shopgroup.goods.http.request.group;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GroupActRequest {

    // 团购id
    private Long id;
    // 团购分类id
    private Long cat;
    // 商品提货方式,1自提2邮递
    @NotNull(message = "提货方式不能为空")
    private Byte pickup;
    // 团购名称
    @NotNull(message = "标题不能为空")
    private String name;
    // 团购介绍
    private String info;
    // 虚拟订单数量
    private Integer virtual;
    // 团购标签id,0=未选择
    private Long tagId;
    //活动开始时间
    @NotNull(message = "活动开始时间不能为空")
    private Integer startTime;
    //活动结束时间
    @NotNull(message = "活动结束时间不能为空")
    private Integer endTime;
    // 商品列表
    @NotEmpty(message = "团购商品不能为空")
    List<GroupActGoodsRequest> goods;

}
