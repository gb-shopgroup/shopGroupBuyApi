package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 自提点员工信息表
@Data
@TableName("gb_org_point_staff")
public class GbOrgPointStaff {

    // 自提点id,主键/外键
    @TableField("point_id")
    private Long pointId;
    // 员工id,主键/外键
    @TableField("staff_id")
    private Long staffId;

}
