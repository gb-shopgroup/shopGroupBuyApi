package cn.com.shopgroup.exception;

// 缺少token令牌或者令牌错误的自定义异常
public class TokenException extends RuntimeException {

    protected final String message;

    // 构造方法
    public TokenException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
