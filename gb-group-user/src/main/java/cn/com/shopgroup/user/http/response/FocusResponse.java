package cn.com.shopgroup.user.http.response;

import cn.com.shopgroup.user.model.GbFocusInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FocusResponse {

    private Long id;
    private String title;
    private String url;
    private String link;

    public FocusResponse(){

    }

    public FocusResponse(GbFocusInfo data){

        this.id = data.getFocusId();
        this.title = data.getFocusTitle();
        this.url = data.getFocusImg();
        this.link = data.getFocusLink();
    }

    // 列表转换
    public static List<FocusResponse> getFocusResponseList(List<GbFocusInfo> lists){

        List<FocusResponse> data = new ArrayList<>();
        for(GbFocusInfo item : lists){
            data.add(new FocusResponse(item));
        }
        return data;
    }

}
