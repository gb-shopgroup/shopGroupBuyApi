package cn.com.shopgroup.user.http.response;

import cn.com.shopgroup.user.model.GbMemberAddressInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AddressResponse {

    private String name;
    private String telephone;

    private String province;
    private String city;
    private String district;
    private String address;

    public AddressResponse(){

    }

    public AddressResponse(GbMemberAddressInfo data){

        this.name = data.getTrueName();
        this.telephone = data.getTelephone();
        this.province = data.getProvince();
        this.city = data.getCity();
        this.district = data.getDistrict();
        this.address = data.getAddress();
    }

    // 列表转换
    public static List<AddressResponse> getAddressResponseList(List<GbMemberAddressInfo> lists){

        List<AddressResponse> data = new ArrayList<>();
        for(GbMemberAddressInfo item : lists){
            data.add(new AddressResponse(item));
        }
        return data;
    }

}
