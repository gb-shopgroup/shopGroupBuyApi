package cn.com.shopgroup.order.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 团购查看记录表：用户浏览团购详情的埋点数据
 * 用于团长端"我的团员"中查看次数与查看动态
 */
@Data
@TableName("gb_group_view_log")
public class GbGroupViewLog {

    // 记录id,主键自增
    @TableId(type = IdType.AUTO)
    private Long id;
    // 浏览用户id,外键
    @TableField("member_id")
    private Long memberId;
    // 团长id,外键,冗余
    @TableField("leader_id")
    private Long leaderId;
    // 团购id,外键
    @TableField("group_id")
    private Long groupId;
    // 团购名称,冗余
    @TableField("group_name")
    private String groupName;
    // 手机号码,冗余
    @TableField("mobile")
    private String mobile;
    // 微信昵称,冗余
    @TableField("nickname")
    private String nickname;
    // 微信头像,冗余
    @TableField("avatar")
    private String avatar;
    // 浏览时间,秒级时间戳
    @TableField("view_time")
    private Integer viewTime;
}
