package cn.com.shopgroup.common.exception;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.RequestLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器（统一收口，原各业务模块重复的 GlobalExceptionHandler 已迁移至此）
 *
 * <p>全局捕获异常 -> 统一封装 JsonResult -> 记录请求日志 -> 返回标准化 JSON。
 * 异常处理顺序遵循「精确匹配优先，Exception 兜底」，覆盖：
 * <ul>
 *   <li>{@link BusinessException} 业务异常：返回业务方指定的 code / 提示语（默认 code=300）</li>
 *   <li>{@link TokenException} 鉴权异常：返回无权限(400)，提示语携带具体缺失/过期原因</li>
 *   <li>参数绑定 / 参数校验（表单、@RequestParam、@RequestBody）异常：定位到具体字段/参数名</li>
 *   <li>请求方法不支持 / 请求体解析失败 / 上传文件超限等框架级异常</li>
 *   <li>{@link Exception} 兜底：返回根因摘要 + 出错方法定位，避免笼统"系统繁忙"导致无法排查</li>
 * </ul>
 *
 * <p>注意：各应用启动类位于 cn.com.shopgroup 根包，本类（cn.com.shopgroup.common.exception）
 * 会被默认组件扫描自动注册为全局异常处理器，业务模块无需再各自实现一份。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 兜底返回给前端的根因摘要最大长度，防止刷爆消息 */
    private static final int MAX_ROOT_MSG_LEN = 160;

    // ==================== 业务异常 ====================

    // 业务异常：预期内的失败，返回业务方指定 code + 提示语，warn 日志即可（不打堆栈避免噪声）
    @ExceptionHandler(BusinessException.class)
    public JsonResult handler(BusinessException e) {
        log.warn("业务异常 code={}, message={}", e.getCode(), e.getMessage());
        return new JsonResult(e.getCode(), e.getMessage());
    }

    // Token 缺失或非法：返回无权限(400)，并用异常自身信息提示具体原因（缺失/过期/非法）
    @ExceptionHandler(TokenException.class)
    public JsonResult handler(TokenException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "缺少token令牌或者令牌错误", e);
        String message = (e.getMessage() == null || e.getMessage().trim().isEmpty())
                ? "没有权限"
                : e.getMessage();
        return new JsonResult(JsonResult.AUTH_CODE, message);
    }

    // ==================== 参数校验异常 ====================

    // 参数绑定异常（表单参数绑定失败），返回校验注解提示，未配置 message 时带出字段名
    @ExceptionHandler(BindException.class)
    public JsonResult handler(BindException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "参数绑定异常", e);
        return JsonResult.error(buildFieldErrorMsg(e.getBindingResult().getFieldErrors()));
    }

    // 参数校验异常（@RequestParam / @PathVariable 校验失败）
    @ExceptionHandler(ConstraintViolationException.class)
    public JsonResult handler(ConstraintViolationException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "参数校验异常", e);
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return JsonResult.error(message);
    }

    // @RequestBody 实体校验失败（@Valid / @Validated）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JsonResult handler(MethodArgumentNotValidException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "请求体校验异常", e);
        return JsonResult.error(buildFieldErrorMsg(e.getBindingResult().getFieldErrors()));
    }

    // 缺少必填请求参数（@RequestParam(required=true) 未传）
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public JsonResult handler(MissingServletRequestParameterException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "缺少必填请求参数", e);
        return JsonResult.error("缺少必填参数: " + e.getParameterName());
    }

    // 请求参数类型不匹配（如传字符串给 Long / Date 类型参数）
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public JsonResult handler(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "请求参数类型错误", e);
        String expect = e.getRequiredType() == null ? "" : "，应为 " + e.getRequiredType().getSimpleName();
        String message = "参数[" + e.getName() + "]类型不合法" + expect;
        return JsonResult.error(message);
    }

    // ==================== 框架级异常 ====================

    // 请求url不存在错误
    @ExceptionHandler(NoHandlerFoundException.class)
    public JsonResult handler(NoHandlerFoundException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "请求路径异常", e);
        return JsonResult.error("请求路径不存在: " + e.getRequestURL());
    }

    // 请求方法不支持（GET/POST 不匹配）
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public JsonResult handler(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "请求方法不支持", e);
        String message = "请求方式不支持: " + e.getMethod()
                + ", 请使用: " + e.getSupportedHttpMethods();
        return JsonResult.error(message);
    }

    // 请求体不存在或格式错误（JSON 解析失败等），带出根因便于定位具体字段
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public JsonResult handler(HttpMessageNotReadableException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "请求体解析异常", e);
        String detail = rootMessage(e);
        if (detail == null || detail.trim().isEmpty()) {
            detail = "请求体缺失或为空";
        }
        return JsonResult.error("请求体解析失败: " + truncate(detail, MAX_ROOT_MSG_LEN));
    }

    // 请求 Content-Type 不支持
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public JsonResult handler(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "请求Content-Type不支持", e);
        String message = "不支持的请求Content-Type: "
                + (e.getContentType() == null ? "无" : e.getContentType())
                + ", 请使用: " + e.getSupportedMediaTypes();
        return JsonResult.error(message);
    }

    // 上传文件超过大小限制
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public JsonResult handler(MaxUploadSizeExceededException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "上传文件超过大小限制", e);
        long maxBytes = e.getMaxUploadSize();
        String message = maxBytes > 0
                ? "上传文件超过大小限制, 最大允许 " + maxBytes / 1024 / 1024 + "MB"
                : "上传文件超过大小限制";
        return JsonResult.error(message);
    }

    // 兜底异常：捕获所有未处理异常，避免返回 Spring 默认 500 错误页。
    // 返回根因摘要 + 方法定位，避免千篇一律"系统繁忙"导致问题无法定位；
    // 完整堆栈仍由日志保留，可按定位行号直接排查。
    @ExceptionHandler(Exception.class)
    public JsonResult handler(Exception e, HttpServletRequest request) {
        String location = resolveLocation(e);
        RequestLogUtils.logRequestError(request, "系统异常, 定位=" + location, e);
        return JsonResult.error(buildFallbackMessage(e, location));
    }

    // ==================== 提示语构建 ====================

    /**
     * 参数绑定字段错误拼接：
     * 注解配置了中文业务 message 时原样返回；
     * 未配置 message 或为框架默认提示时补充字段名，避免出现难以定位的默认英文提示
     */
    private String buildFieldErrorMsg(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(error -> {
                    String defaultMessage = error.getDefaultMessage();
                    if (defaultMessage == null || defaultMessage.trim().isEmpty()) {
                        return error.getField() + " 参数不合法";
                    }
                    defaultMessage = defaultMessage.trim();
                    // 业务已配置 message（一般含中文提示语），按配置原文返回
                    if (defaultMessage.matches(".*[\\u4e00-\\u9fa5].*")) {
                        return defaultMessage;
                    }
                    // 框架默认校验提示（多为英文），补充字段名便于定位
                    return error.getField() + " " + defaultMessage;
                })
                .distinct()
                .collect(Collectors.joining("; "));
    }

    /** 兜底提示语：系统异常 + 根因摘要 + 出错位置（类.方法:行号） */
    private String buildFallbackMessage(Throwable e, String location) {
        String detail = rootMessage(e);
        if (detail == null || detail.trim().isEmpty()) {
            detail = rootCause(e).getClass().getSimpleName();
        } else {
            detail = truncate(detail.trim(), MAX_ROOT_MSG_LEN);
        }
        StringBuilder message = new StringBuilder("系统异常: ").append(detail);
        if (location != null && !location.isEmpty()) {
            message.append("; 定位: ").append(location);
        }
        return message.toString();
    }

    /** 超长内容截断，防止大字段/堆栈信息刷爆返回消息 */
    private String truncate(String value, int maxLen) {
        if (value == null || value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen) + "...(已截断)";
    }

    /** 递归取最底层根因的 message（无则 null） */
    private String rootMessage(Throwable e) {
        Throwable root = rootCause(e);
        return root == null ? null : root.getMessage();
    }

    private Throwable rootCause(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * 从异常栈中定位项目业务代码第一个栈帧（cn.com.shopgroup 包），
     * 优先从根因处找，找不到再退回最外层异常栈，输出形如 Class.method(File.java:123)
     */
    private String resolveLocation(Throwable e) {
        Throwable root = rootCause(e);
        String frame = findFirstBizFrame(root.getStackTrace());
        if (frame == null) {
            frame = findFirstBizFrame(e.getStackTrace());
        }
        return frame == null ? "" : frame;
    }

    private String findFirstBizFrame(StackTraceElement[] stackTrace) {
        if (stackTrace == null) {
            return null;
        }
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className == null || !className.startsWith("cn.com.shopgroup")) {
                continue;
            }
            String shortName = className.substring(className.lastIndexOf('.') + 1);
            String position = element.getFileName() == null
                    ? "行" + element.getLineNumber()
                    : element.getFileName() + ":" + element.getLineNumber();
            return shortName + "." + element.getMethodName() + "(" + position + ")";
        }
        return null;
    }
}
