package cn.com.shopgroup.user.utils;

import cn.com.shopgroup.common.utils.IntEncryptorUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class RequestParamsUtils {

    // 从请求头中获取团长id
    public static Long getRequestHeaderLeaderId(){

        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        String params = request.getHeader("lid");
        if(null != params && params.length() > 0){
            return (long)IntEncryptorUtils.decrypt(params);
        }else{
            return 0l;
        }
    }

    // 从请求头中获取店员id
    public static Long getRequestHeaderStaffId(){

        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        String params = request.getHeader("sid");
        if(null != params && params.length() > 0){
            return (long)IntEncryptorUtils.decrypt(params);
        }else{
            return 0l;
        }
    }
}
