package cn.com.shopgroup.order.http.response;

import lombok.Data;

@Data
public class GroupLogs {

    private String userAvatar = "";
    private String userMobile = "";
    private String userTime = "";
    private String userGoodsName = "";
    private String userGoodsNum = "";

    public GroupLogs(){

    }

    public GroupLogs(String avatar, String mobile, String time, String name, String num){

        this.userAvatar = avatar;
        this.userMobile = mobile;
        this.userTime = time;
        this.userGoodsName = name;
        this.userGoodsNum = num;
    }

}
