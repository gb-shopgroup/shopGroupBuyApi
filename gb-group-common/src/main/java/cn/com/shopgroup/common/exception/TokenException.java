package cn.com.shopgroup.common.exception;

/**
 * Token 缺失或非法异常
 *
 * 由拦截器 / 鉴权层抛出，common 统一全局异常处理器捕获后返回无权限(400)。
 * 从 gb-group-admin 提升为公共异常，供各模块 / 网关鉴权统一复用。
 */
public class TokenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TokenException(String message) {
        super(message);
    }
}
