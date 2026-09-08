package cn.com.shopgroup.user.http.response;

import cn.com.shopgroup.user.model.GbOrgPointInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PointResponse {
    //自提点id
    private Long pointId;
    // 团长id,外键
    private Long leaderId;
    // 自提点名称
    private String pointName;
    // 详细地址
    private String pointAddress;
    // 门头照片
    private String pointImg;
    // 经度,精度为10米级
    private Double longitude;
    // 纬度,精度为10米级
    private Double latitude;
    // 自提范围,单位：公里
    private Integer pointScope;
    // 自提说明,给c端用户看的
    private String pointInfo;
    // 小程序二维码,给c端用户扫码使用
    private String pointErcode;
    //联系人
    private String person;
    //联系电话
    private String phone;
    // 是否禁用
    private Byte isClose;

    public PointResponse() {

    }

    public PointResponse(GbOrgPointInfo data) {

        this.pointId = data.getPointId();
        this.pointName = data.getPointName();
        this.pointAddress = data.getPointAddress();
        this.person = data.getPerson();
        this.phone = data.getPhone();
        this.latitude = data.getLatitude();
        this.longitude = data.getLongitude();
        this.isClose = data.getIsClose();
        this.pointErcode = data.getPointErcode();
        this.pointInfo = data.getPointInfo();
        this.pointImg = data.getPointImg();
        this.leaderId = data.getLeaderId();
        this.pointScope = data.getPointScope();
    }

    // 列表转换
    public static List<PointResponse> getPointResponseList(List<GbOrgPointInfo> lists) {

        List<PointResponse> data = new ArrayList<>();
        for (GbOrgPointInfo item : lists) {
            data.add(new PointResponse(item));
        }
        return data;
    }

}
