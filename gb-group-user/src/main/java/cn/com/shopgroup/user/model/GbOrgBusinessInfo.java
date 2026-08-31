package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团长分账户信息表
@Data
@TableName("gb_org_business_info")
public class GbOrgBusinessInfo {

    // 账户id,主键自增
    @TableId(type = IdType.AUTO)
    private Long busId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 账户类型,1=一般企业2=小微企业3=个体户4=个人
    @TableField("bus_type")
    private Byte busType;
    // 账户名称
    @TableField("bus_name")
    private String busName;
    // 法人姓名
    @TableField("legal_name")
    private String legalName;
    // 身份证号
    @TableField("cid_no")
    private String cidNo;
    // 身份证正面
    @TableField("cid_front")
    private String cidFront;
    // 身份证反面
    @TableField("cid_back")
    private String cidBack;
    // 营业执照号
    @TableField("license_no")
    private String licenseNo;
    // 营业执照正面
    @TableField("license_front")
    private String licenseFront;
    // 营业执照反面
    @TableField("license_back")
    private String licenseBack;
    // 账户余额
    @TableField("bus_balance")
    private Double busBalance;
    //限制最高收款金额(万)
    @TableField("limit_amount")
    private Integer limitAmount;
    // 纳税额度,单位：万（收款提醒）
    @TableField("tax_limit")
    private Integer taxLimit;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 是否审核
    @TableField("is_check")
    private Byte isCheck;
    // 审核备注
    @TableField("check_remark")
    private String checkRemark;
    // 商户支付ID(审核通过后给予)
    @TableField("check_cust_id")
    private String checkCustId;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;


}
