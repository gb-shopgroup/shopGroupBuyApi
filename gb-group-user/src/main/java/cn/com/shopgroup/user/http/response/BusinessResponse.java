package cn.com.shopgroup.user.http.response;

import cn.com.shopgroup.user.model.GbOrgBusinessInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BusinessResponse {

    // 账户id
    private Long id;

    // 账户类型, 1=一般企业2=小微企业3=个体户4=个人
    private Byte type;
    // 账户名称
    private String name;
    // 法人姓名
    private String legal;
    // 身份证号
    private String no;
    // 身份证正面
    private String front;
    // 身份证反面
    private String back;
    // 纳税额度,单位：万（收款提醒）
    private Integer tax;
    // 余额
    private Double balance;
    // 是否禁用
    private Byte isClose;

    // 是否审核
    private Byte isCheck;


    public BusinessResponse() {

    }

    public BusinessResponse(GbOrgBusinessInfo info) {

        this.id = info.getBusId();
        this.type = info.getBusType();
        this.name = info.getBusName();
        this.legal = info.getLegalName();
        if (this.type == 4) {
            this.no = info.getCidNo();
            this.front = info.getCidFront();
            this.back = info.getCidBack();
        } else {
            this.no = info.getLicenseNo();
            this.front = info.getLicenseFront();
            this.back = info.getLicenseBack();
        }
        this.tax = info.getTaxLimit();
        this.balance = info.getBusBalance();
        this.isClose = info.getIsClose();
        this.isCheck = info.getIsCheck();
    }

    // 列表转换
    public static List<BusinessResponse> getBusinessResponseList(List<GbOrgBusinessInfo> lists) {

        List<BusinessResponse> data = new ArrayList<>();
        for (GbOrgBusinessInfo item : lists) {
            data.add(new BusinessResponse(item));
        }
        return data;
    }
}
