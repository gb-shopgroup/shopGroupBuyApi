package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团长消息表
@Data
@TableName("gb_org_message_info")
public class GbOrgMessageInfo {

    // 消息id,主键自增
    @TableId(type = IdType.AUTO)
    private Long msgId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 员工id,外键
    @TableField("staff_id")
    private Long staffId;
    // 消息类型,1=系统消息2=内部消息3=业务消息
    @TableField("msg_type")
    private Byte msgType;
    // 消息内容
    @TableField("msg_content")
    private String msgContent;
    // 是否阅读
    @TableField("is_read")
    private Byte isRead;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
