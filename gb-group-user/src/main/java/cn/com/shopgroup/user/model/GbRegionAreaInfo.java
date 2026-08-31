package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 行政区域信息表
@Data
@TableName("gb_region_area_info")
public class GbRegionAreaInfo {

    // 区域id,主键（自主编码）
    @TableId(type = IdType.AUTO)
    private Long regionId;
    // 上级区域,外键
    @TableField("region_parent")
    private Long regionParent;
    // 区域名称
    @TableField("region_name")
    private String regionName;
    // 区域坐标
    @TableField("center")
    private String center;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 地图定位id,外键
    @TableField("map_id")
    private Long mapId;
    // 地图定位名称,冗余
    @TableField("map_name")
    private String mapName;

}
