package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

// 用户建议反馈表
@Data
@TableName("gb_member_advise_feedback")
public class GbMemberAdviseFeedback {

    // 主键自增id
    @TableId(type = IdType.AUTO)
    private Long id;
    // 反馈标题
    @TableField("title")
    private String title;
    // 反馈内容
    @TableField("content")
    private String content;
    // 提交用户id
    @TableField("member_id")
    private Long memberId;
    // 处理状态: 0-待处理, 1-处理中, 2-已解决, 3-已关闭
    @TableField("status")
    private Integer status;
    // 官方回复内容
    @TableField("reply")
    private String reply;
    // 创建时间
    @TableField("add_time")
    private Date addTime;
    // 更新时间
    @TableField("update_time")
    private Date updateTime;

}
