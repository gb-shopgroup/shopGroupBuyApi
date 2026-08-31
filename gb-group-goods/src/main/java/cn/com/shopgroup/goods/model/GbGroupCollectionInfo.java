package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团购合集信息表
@Data
@TableName("gb_group_collection_info")
public class GbGroupCollectionInfo {

    // 合集id,主键自增
    @TableId(type = IdType.AUTO)
    private Long collId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 数据隔离id
    @TableField("isolation_id")
    private Integer isolationId;
    // 合集名称
    @TableField("coll_name")
    private String collName;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加人员id
    @TableField("staff_id")
    private Long staffId;
    // 添加人员姓名
    @TableField("staff_name")
    private String staffName;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
