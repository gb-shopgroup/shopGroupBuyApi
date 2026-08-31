package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 用户/会员地址表
@Data
@TableName("gb_member_address_info")
public class GbMemberAddressInfo {

    // 地址id,主键自增
    @TableId(type = IdType.AUTO)
    private Long addressId;
    // 用户id,外键
    @TableField("member_id")
    private Long memberId;
    // 收货姓名
    @TableField("true_name")
    private String trueName;
    // 收货电话
    @TableField("telephone")
    private String telephone;
    // 所属省份
    @TableField("province")
    private String province;
    // 所属城市
    @TableField("city")
    private String city;
    // 所属区县
    @TableField("district")
    private String district;
    // 详细地址
    @TableField("address")
    private String address;
    // 是否默认
    @TableField("is_default")
    private Byte isDefault;

}
