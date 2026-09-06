package cn.com.shopgroup.user.utils;

import cn.com.shopgroup.common.utils.IntEncryptorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class RequestParamsUtils {

    private static final Logger log = LoggerFactory.getLogger(RequestParamsUtils.class);

    // 从请求头中获取团长id
    public static Long getRequestHeaderLeaderId(){

        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        String params = request.getHeader("lid");
        if(null != params && params.length() > 0){
            // 优先解密(登录接口下发的加密lid), 兼容明文数字(联调/网关透传场景)
            try {
                return (long)IntEncryptorUtils.decrypt(params);
            } catch (Exception e) {
                // 解密失败(非url-safe base64), 尝试按明文数字解析
                try {
                    return Long.parseLong(params.trim());
                } catch (Exception ex) {
                    log.warn("请求头lid非法(既非加密串也非数字),原始lid:{},msg:{}", params, ex.getMessage());
                    return 0L;
                }
            }
        }else{
            return 0l;
        }
    }

    // 从请求头中获取店员id
    public static Long getRequestHeaderStaffId(){

        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        String params = request.getHeader("sid");
        if(null != params && params.length() > 0){
            // 优先解密(登录接口下发的加密sid), 兼容明文数字(联调/网关透传场景)
            try {
                return (long)IntEncryptorUtils.decrypt(params);
            } catch (Exception e) {
                // 解密失败(非url-safe base64), 尝试按明文数字解析
                try {
                    return Long.parseLong(params.trim());
                } catch (Exception ex) {
                    log.warn("请求头sid非法(既非加密串也非数字),原始sid:{},msg:{}", params, ex.getMessage());
                    return 0L;
                }
            }
        }else{
            return 0l;
        }
    }
}
