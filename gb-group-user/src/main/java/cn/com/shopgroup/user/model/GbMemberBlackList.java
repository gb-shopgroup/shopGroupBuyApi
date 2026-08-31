package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 用户黑名单
@Data
@TableName("gb_member_blacklist")
public class GbMemberBlackList {
    @TableId(type = IdType.AUTO)
    private Long id;
    // 团长id
    @TableField("leader_id")
    private Long leaderId;
    // 用户id
    @TableField("member_id")
    private Long memberId;
    // 手机号码
    @TableField("mobile")
    private String mobile;
    // 微信昵称
    @TableField("nickname")
    private String nickName;
    // 微信头像
    @TableField("avatar")
    private String avatar;
    // 有效时间
    @TableField("expire_time")
    private Integer expireTime;
    // 添加人id
    @TableField("staff_id")
    private Long staffId;
    // 添加人姓名
    @TableField("staff_name")
    private String staffName;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
