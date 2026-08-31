package cn.com.shopgroup.order.http.response;

import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.order.model.GbReportBusinessInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReportResponse {

    private Long id;
    private String name;
    private String title;
    private Double total;

    public ReportResponse() {

    }

    public ReportResponse(GbReportBusinessInfo data) {

        this.id = data.getBusId();
        this.name = data.getBusName();
        this.title = data.getReportName();
        this.total = MoneyUtil.centToYuan(data.getBusFee());
    }

    // 列表转化
    public static List<ReportResponse> getReportResponseList(List<GbReportBusinessInfo> lists) {

        List<ReportResponse> data = new ArrayList<>();
        for (GbReportBusinessInfo item : lists) {
            data.add(new ReportResponse(item));
        }
        return data;
    }
}
