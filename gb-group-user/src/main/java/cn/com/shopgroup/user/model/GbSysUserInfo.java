package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 系统用户信息表
@Data
@TableName("gb_sys_user_info")
public class GbSysUserInfo {

    // 用户id,主键自增
    @TableId(type = IdType.AUTO)
    private Long userId;
    // 登录账号,唯一约束
    @TableField("user_name")
    private String userName;
    // 登录密码
    @TableField("pass_word")
    private String passWord;
    // 用户姓名
    @TableField("true_name")
    private String trueName;
    // 用户头像
    @TableField("avatar")
    private String avatar;
    // 用户备注
    @TableField("remark")
    private String remark;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
