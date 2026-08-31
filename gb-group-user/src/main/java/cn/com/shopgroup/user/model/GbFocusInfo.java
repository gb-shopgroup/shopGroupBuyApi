package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 轮播图信息表
@Data
@TableName("gb_focus_info")
public class GbFocusInfo {

    // 轮播id,主键自增
    @TableId(type = IdType.AUTO)
    private Long focusId;
    // 轮播标题
    @TableField("focus_title")
    private String focusTitle;
    // 轮播图片
    @TableField("focus_img")
    private String focusImg;
    // 链接地址,小程序页面地址
    @TableField("focus_link")
    private String focusLink;
    // 排列顺序
    @TableField("sort_order")
    private Byte sortOrder;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
