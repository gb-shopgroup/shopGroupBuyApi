package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 自提点区域信息表
@Data
@TableName("gb_org_point_region")
public class GbOrgPointRegion {

    // 自提点id,主键/外键
    @TableField("point_id")
    private Long pointId;
    // 区域id,主键/外键
    @TableField("region_id")
    private Long regionId;

}
