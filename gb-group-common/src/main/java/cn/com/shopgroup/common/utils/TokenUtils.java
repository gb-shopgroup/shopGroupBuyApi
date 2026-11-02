package cn.com.shopgroup.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

// 令牌工具类
@Slf4j
public class TokenUtils {

    // 令牌密钥
    private final static String userkey = "userId";
    private final static String secret = "ssf4oain2addou76hkv4umf3e";

    // 令牌标识
    private final static String header = "Authorization";

    // 生成Token令牌，传递用户 phone 信息
    public static String createToken(String userId) {

        Map<String, Object> claims = new HashMap<>();
        claims.put(userkey, userId);
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // 解析Token令牌，获取用 userId 信息
    public static String parseToken(String token) {
        log.info("解析Token调用parseToken时，token:{}",token);
        try {
            Claims userInfo = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            log.info("解析Token调用parseToken时，解析结果userInfo:{}",userInfo);
            String result = userInfo.get(userkey).toString();
            return result;
        } catch (Exception e){
            //e.printStackTrace();
            return "";
        }
    }

    // 从请求头中获取Token
    public static String getToken(){

        // 从Header中获取token
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        String token = request.getHeader(header);
        log.info("从request调用getToken时，token:{}",token);
        if (null == token || "".equals(token.trim())) return "";
        return token;
    }

}

