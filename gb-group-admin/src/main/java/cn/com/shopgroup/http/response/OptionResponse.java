package cn.com.shopgroup.http.response;

import lombok.Data;

@Data
public class OptionResponse {

    private int value;
    private String label;

    public OptionResponse(){

    }

    public OptionResponse(int id, String name){

        this.value = id;
        this.label = name;
    }

}
