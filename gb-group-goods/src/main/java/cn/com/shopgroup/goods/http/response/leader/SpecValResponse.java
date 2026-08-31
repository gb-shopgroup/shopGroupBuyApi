package cn.com.shopgroup.goods.http.response.leader;

import cn.com.shopgroup.goods.model.GbGoodsSpecValue;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SpecValResponse {

    // 规格值id
    private Long id;
    // 规格值
    private String val;
    // 是否禁用
    private Byte isClose;

    public SpecValResponse() {

    }

    public SpecValResponse(GbGoodsSpecValue data) {

        this.id = data.getValId();
        this.val = data.getSpecVal();
        this.isClose = data.getIsClose();
    }

    // 列表转换
    public static List<SpecValResponse> getSpecValResponseList(List<GbGoodsSpecValue> lists) {

        List<SpecValResponse> data = new ArrayList<>();
        for (GbGoodsSpecValue item : lists) {
            data.add(new SpecValResponse(item));
        }
        return data;
    }
}
