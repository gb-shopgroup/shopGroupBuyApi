package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 系统配置信息表
@Data
@TableName("gb_sys_config_info")
public class GbSysConfigInfo {

    // 配置id,主键自增
    @TableId(type = IdType.AUTO)
    private Long configId;
    // 配置项目,英文
    @TableField("config_key")
    private String configKey;
    // 配置名称,中文
    @TableField("config_name")
    private String configName;
    // 配置数值,json格式
    @TableField("config_value")
    private String configValue;
    // 是否编辑,1可以编辑
    @TableField("is_edit")
    private Byte isEdit;
    // 配置备注
    @TableField("remark")
    private String remark;

}
