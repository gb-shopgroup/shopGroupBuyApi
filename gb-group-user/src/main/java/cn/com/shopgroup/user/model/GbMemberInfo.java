package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 用户/会员信息表
@Data
@TableName("gb_member_info")
public class GbMemberInfo {

    // 用户id,主键自增
    @TableId(type = IdType.AUTO)
    private Long memberId;
    // 手机号码
    @TableField("mobile")
    private String mobile;
    // 微信昵称
    @TableField("nickname")
    private String nickname;
    // 微信头像
    @TableField("avatar")
    private String avatar;
    // openid,唯一索引（自动登录）
    @TableField("openid")
    private String openid;
    // 地图定位id,外键
    @TableField("map_id")
    private Long mapId;
    // 地图定位名称
    @TableField("map_name")
    private String mapName;
    // 团长id,外键/来源
    @TableField("leader_id")
    private Long leaderId;
    // 数据隔离id
    @TableField("isolation_id")
    private Integer isolationId;
    // 小程序二维码,给团长扫码使用
    @TableField("ercode")
    private String ercode;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
