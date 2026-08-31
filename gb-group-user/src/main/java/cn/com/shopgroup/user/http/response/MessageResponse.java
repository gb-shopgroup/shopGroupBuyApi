package cn.com.shopgroup.user.http.response;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.user.model.GbOrgMessageInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageResponse {

    // 消息id
    private Long id;
    // 消息类型,1=系统消息2=内部消息3=业务消息
    private Byte type;
    // 消息内容
    private String content;
    // 是否阅读
    private Byte read;
    // 添加时间
    private String time;

    public MessageResponse(){

    }

    public MessageResponse(GbOrgMessageInfo data){

        this.id = data.getMsgId();
        this.type = data.getMsgType();
        this.content = data.getMsgContent();
        this.read = data.getIsRead();
        this.time = TimeUtils.getFormatTimeStamp(data.getAddTime());
    }

    // 列表转化
    public static List<MessageResponse> getMessageResponseList(List<GbOrgMessageInfo> lists){

        List<MessageResponse> data = new ArrayList<>();
        for(GbOrgMessageInfo item : lists){
            data.add(new MessageResponse(item));
        }
        return data;
    }
}
