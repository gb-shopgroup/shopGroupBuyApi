package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团购区域表
@Data
@TableName("gb_group_region_info")
public class GbGroupRegionInfo {

    // 团购id,主键/外键
    @TableField("group_id")
    private Long groupId;
    // 区域id,主键/外键
    @TableField("region_id")
    private Long regionId;

}
