package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

// 申请团长记录信息表
@Data
@TableName("gb_apply_leader_record")
public class GbApplyLeaderRecord {
    // 主键自增id
    @TableId(type = IdType.AUTO)
    private Long id;
    // 姓名
    @TableField("name")
    private String name;
    // 手机号码(唯一)
    @TableField("mobile")
    private String mobile;
    // 申请状态 0 申请中 1 申请成功 2 申请失败
    @TableField("apply_status")
    private Integer applyStatus;
    // 申请时间
    @TableField("add_time")
    private Date addTime;
    // 修改时间
    @TableField("update_time")
    private Date updateTime;

}
