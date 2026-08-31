package cn.com.shopgroup.user.http.response;

import cn.com.shopgroup.user.model.GbOrgPointInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PointResponse {

    private Long id;
    private String name;
    private String address;
    private String person;
    private String phone;

    public PointResponse() {

    }

    public PointResponse(GbOrgPointInfo data) {

        this.id = data.getPointId();
        this.name = data.getPointName();
        this.address = data.getPointAddress();
        this.person = data.getPerson();
        this.phone = data.getPhone();
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
