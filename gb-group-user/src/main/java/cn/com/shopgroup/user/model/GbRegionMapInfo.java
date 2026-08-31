package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 地图区域信息表
@Data
@TableName("gb_region_map_info")
public class GbRegionMapInfo {

    // 地图定位id,主键自增
    @TableId(type = IdType.AUTO)
    private Long mapId;
    // 地图定位名称
    @TableField("map_name")
    private String mapName;
    // 地图完整地址
    @TableField("map_address")
    private String mapAddress;
    // 区域id,外键
    @TableField("region_id")
    private Long regionId;
    // 区域名称,冗余
    @TableField("region_name")
    private String regionName;

}
