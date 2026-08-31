package cn.com.shopgroup.common.utils;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

public class IpUtils {

    public static String getClientIp() {

        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }
        return parseIp(request);
    }

    private static HttpServletRequest getRequest() {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }

    private static String parseIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            ip = ip.split(",")[0].trim();
            return ip;
        }

        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) return ip;
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) return ip;
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) return ip;

        ip = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    private static boolean isValidIp(String ip) {

        return StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip);
    }

}
