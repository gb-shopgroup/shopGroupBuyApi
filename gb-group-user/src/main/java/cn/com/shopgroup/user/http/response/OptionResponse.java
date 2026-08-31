package cn.com.shopgroup.user.http.response;

import lombok.Data;

@Data
public class OptionResponse {

    private Integer value;
    private String label;

    public OptionResponse(){

    }

    public OptionResponse(Integer id, String name){

        this.value = id;
        this.label = name;
    }

}
