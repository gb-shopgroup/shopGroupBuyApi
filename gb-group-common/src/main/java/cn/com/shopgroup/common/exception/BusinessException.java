package cn.com.shopgroup.common.exception;

import cn.com.shopgroup.common.utils.JsonResult;
import lombok.Getter;

/**
 * 业务异常
 *
 * <p>业务处理失败时抛出（service / controller 均可），由 common 模块的统一
 * {@link GlobalExceptionHandler} 捕获后返回标准 JsonResult，避免层层
 * return JsonResult.fail(...) 的样板代码。
 *
 * <p>默认 code 为 300（等价 {@link JsonResult#fail}），不影响既有前端协议；需要
 * 区分失败类型时可直接传各业务模块错误码枚举（实现 {@link IErrorCode}）或自定义 code。
 *
 * <pre>
 *   throw new BusinessException("库存不足");
 *   throw new BusinessException(ErrorCodeEnum.UNAUTHORIZED);
 *   throw new BusinessException(UserErrorCodeEnum.STAFF_NOT_EXIST);
 *   throw new BusinessException(500, "退款失败");
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 返回给前端的业务状态码
     */
    private final int code;

    public BusinessException(String message) {
        this(JsonResult.FAIL_CODE, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(IErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }
}
