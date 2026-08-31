package cn.com.shopgroup.user.http.response;

import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class StaffResponse {

    // 员工id,主键自增
    private Long id;
    // 员工姓名
    private String name;
    // 手机号码
    private String mobile;
    // 员工备注
    private String remark;
    // 权限设置(逗号间隔)
    private String auth;
    // 是否禁用
    private Byte isClose;

    // 所属提货点(逗号间隔)
    private String point;

    public StaffResponse(){

    }

    public StaffResponse(GbOrgStaffInfo data){

        this.id = data.getStaffId();
        this.name = data.getStaffName();
        this.mobile = data.getMobile();
        this.remark = data.getRemark();
        this.auth = data.getAuthList();
        this.isClose = data.getIsClose();

        List<Long> pointIds = data.getPointList().stream().map(GbOrgPointInfo::getPointId).collect(Collectors.toList());
        if(pointIds == null) pointIds = new ArrayList<>();
        this.point = pointIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    // 列表转化
    public static List<StaffResponse> getStaffResponseList(List<GbOrgStaffInfo> lists){

        List<StaffResponse> data = new ArrayList<>();
        for(GbOrgStaffInfo item : lists){
            data.add(new StaffResponse(item));
        }
        return data;
    }
}
