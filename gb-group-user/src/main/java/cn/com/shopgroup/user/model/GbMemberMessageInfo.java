package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 用户/会员消息表
@Data
@TableName("gb_member_message_info")
public class GbMemberMessageInfo {

    // 消息id,主键自增
    @TableId(type = IdType.AUTO)
    private Long msgId;
    // 用户id,外键
    @TableField("member_id")
    private Long memberId;
    // 消息类型
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
