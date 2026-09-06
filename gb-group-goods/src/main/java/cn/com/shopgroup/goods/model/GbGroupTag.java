package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团购标签配置表
@Data
@TableName("gb_group_tag")
public class GbGroupTag {

    // 标签id,主键自增
    @TableId(type = IdType.AUTO)
    private Long tagId;
    // 标签名称
    @TableField("tag_name")
    private String tagName;
    // 标签颜色,展示用
    @TableField("tag_color")
    private String tagColor;
    // 排序,越小越靠前
    @TableField("sort_order")
    private Integer sortOrder;
    // 状态:1启用0停用
    @TableField("status")
    private Byte status;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;
}
