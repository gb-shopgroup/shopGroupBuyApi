package cn.com.shopgroup.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局通用错误码
 *
 * 与前端统一返回协议 JsonResult 保持一致:
 *   200 成功 / 300 业务失败 / 400 无权限 / 500 系统或未知异常
 *
 * 各业务模块请按项目实际情况维护自己的模块错误码枚举（实现 {@link IErrorCode}，
 * 如 UserErrorCodeEnum / OrderErrorCodeEnum），通用错误码仅作兜底；
 * 抛业务异常时统一 {@code throw new BusinessException(错误码枚举.X)}。
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum implements IErrorCode {

    SUCCESS(200, "操作成功"),

    /** 业务处理失败（等价于 JsonResult.fail） */
    BIZ_ERROR(300, "操作失败"),

    /** Token 缺失、非法或无权限（等价于 JsonResult.forbid） */
    UNAUTHORIZED(400, "没有权限"),

    /** 系统异常 / 未知异常（等价于 JsonResult.error） */
    SYSTEM_ERROR(500, "系统繁忙，请稍后重试");

    /** 返回给前端的业务状态码 */
    private final int code;

    /** 返回给前端的提示信息 */
    private final String message;
}
