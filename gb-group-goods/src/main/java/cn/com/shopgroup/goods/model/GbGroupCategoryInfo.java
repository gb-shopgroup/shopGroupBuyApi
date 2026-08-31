package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团购分类信息表
@Data
@TableName("gb_group_category_info")
public class GbGroupCategoryInfo {

    // 分类id,主键自增
    @TableId(type = IdType.AUTO)
    private Long catId;
    // 分类名称
    @TableField("cat_name")
    private String catName;
    // 排列顺序
    @TableField("sort_order")
    private Byte sortOrder;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加时间
    @TableField("add_time")
    private Long addTime;

}
