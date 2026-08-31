package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 图片库信息表
@Data
@TableName("gb_image_library_info")
public class GbImageLibraryInfo {

    // 图片id
    @TableId(type = IdType.AUTO)
    private Long imgId;
    // 图片类型
    @TableField("img_type")
    private Byte imgType;
    // 图片地址
    @TableField("img_url")
    private String imgUrl;
    // 是否私密
    @TableField("is_private")
    private Byte isPrivate;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
