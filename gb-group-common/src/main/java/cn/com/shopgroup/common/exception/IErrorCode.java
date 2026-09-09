package cn.com.shopgroup.common.exception;

/**
 * 错误码统一访问接口
 *
 * <p>公共错误码枚举 {@link ErrorCodeEnum} 与各业务模块自定义的错误码枚举
 * （如 UserErrorCodeEnum / OrderErrorCodeEnum 等）统一实现本接口后，
 * 均可直接通过 {@code throw new BusinessException(错误码枚举.X)} 抛出，
 * 由全局异常处理器自动翻译为标准 JsonResult 返回。
 */
public interface IErrorCode {

    /** 返回给前端的业务状态码（遵循 JsonResult 协议：300 业务失败 / 400 无权限 / 500 系统异常） */
    int getCode();

    /** 返回给前端的提示信息 */
    String getMessage();
}
