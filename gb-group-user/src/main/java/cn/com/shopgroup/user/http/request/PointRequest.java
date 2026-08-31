package cn.com.shopgroup.user.http.request;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PointRequest {

    // 自提点Id
    private Long id;

    // 自提点名称
    @NotNull(message = "名称不能为空")
    private String name;
    // 详细地址
    @NotNull(message = "地址不能为空")
    private String address;
    // 门头照片
    private String img;
    // 经度,精度为10米级
    private Double lon;
    // 纬度,精度为10米级
    private Double lat;
    // 自提范围,单位：公里
    private Byte scope;
    // 自提说明,给c端用户看的
    private String info;
    //联系人
    @TableField("person")
    private String person;
    //联系电话
    @TableField("phone")
    private String phone;

}
