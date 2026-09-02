package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

// 员工信息表
@Data
@TableName("gb_org_staff_info")
public class GbOrgStaffInfo {

    // 员工id,主键自增
    @TableId(type = IdType.AUTO)
    private Long staffId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 员工姓名
    @TableField("staff_name")
    private String staffName;
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
    // 员工备注
    @TableField("remark")
    private String remark;
    // 权限设置,json格式
    @TableField("auth_list")
    private String authList;
    // 是否禁用0 正常 1 关闭
    @TableField("is_close")
    private Byte isClose;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

    // 所属提货点列表
    @TableField(exist = false)
    private List<GbOrgPointInfo> pointList;



}
