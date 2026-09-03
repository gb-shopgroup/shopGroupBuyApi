package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团长信息表
@Data
@TableName("gb_org_leader_info")
public class GbOrgLeaderInfo {

    // 团长id,主键自增
    @TableId(type = IdType.AUTO)
    private Long leaderId;
    // 区域id,外键
    @TableField("region_id")
    private Long regionId;
    // 区域名称,冗余
    @TableField("region_name")
    private String regionName;
    // 团长类型,1=平台团长2=独立团长
    @TableField("leader_type")
    private Byte leaderType;
    // 数据隔离id,平台团长固定值,独立团长分配唯一值
    @TableField("isolation_id")
    private Integer isolationId;
    // 团长姓名
    @TableField("leader_name")
    private String leaderName;
    // 手机号码
    @TableField("mobile")
    private String mobile;
    // 微信昵称
    @TableField("nickname")
    private String nickname;
    // 微信头像
    @TableField("avatar")
    private String avatar;
    // openid,唯一（自动登录）
    @TableField("openid")
    private String openid;
    // 开始时间,使用有效期
    @TableField("start_time")
    private Integer startTime;
    // 结束时间,使用有效期
    @TableField("end_time")
    private Integer endTime;
    // 账户余额
    @TableField("leader_balance")
    private Double leaderBalance;
    // 平台抽成,千分率
    @TableField("commission")
    private Byte commission;
    // 结算到账方式,默认0=支付时延迟到账型,1=核销时延迟到账型
    @TableField("cash_type")
    private Integer cashType;
    // 发团数量限制（状态：在线）
    @TableField("num_limit")
    private Integer numLimit;
    // 系统备注
    @TableField("remark")
    private String remark;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
