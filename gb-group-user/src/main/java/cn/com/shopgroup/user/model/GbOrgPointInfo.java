package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 自提点信息表
@Data
@TableName("gb_org_point_info")
public class GbOrgPointInfo {

    // 自提点id,主键自增
    @TableId(type = IdType.AUTO)
    private Long pointId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 自提点名称
    @TableField("point_name")
    private String pointName;
    // 详细地址
    @TableField("point_address")
    private String pointAddress;
    // 门头照片
    @TableField("point_img")
    private String pointImg;
    // 经度,精度为10米级
    @TableField("longitude")
    private Double longitude;
    // 纬度,精度为10米级
    @TableField("latitude")
    private Double latitude;
    // 自提范围,单位：公里
    @TableField("point_scope")
    private Byte pointScope;
    // 自提说明,给c端用户看的
    @TableField("point_info")
    private String pointInfo;
    // 小程序二维码,给c端用户扫码使用
    @TableField("point_ercode")
    private String pointErcode;
    //联系人
    @TableField("person")
    private String person;
    //联系电话
    @TableField("phone")
    private String phone;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
