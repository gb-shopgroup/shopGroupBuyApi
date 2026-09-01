package cn.com.shopgroup.exception;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.RequestLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一捕获 Controller 层异常并返回 JsonResult 业务格式，
 * 避免异常直接抛出导致 Spring 默认 500 错误页（前端拿不到具体错误信息）。
 * 发生异常时通过 RequestLogUtils 打印请求方式、URI 与请求参数，便于定位问题。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 参数绑定异常（表单参数绑定失败）
    @ExceptionHandler(BindException.class)
    public JsonResult handler(BindException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "参数绑定异常", e);
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return JsonResult.error(message);
    }

    // 参数校验异常（@RequestParam/@PathVariable 校验失败）
    @ExceptionHandler(ConstraintViolationException.class)
    public JsonResult handler(ConstraintViolationException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "参数校验异常", e);
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return JsonResult.error(message);
    }

    // @RequestBody 实体校验失败（@Valid）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JsonResult handler(MethodArgumentNotValidException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "请求体校验异常", e);
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return JsonResult.error(message);
    }

    // 请求方法不支持（GET/POST 不匹配）
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public JsonResult handler(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "请求方法不支持", e);
        return JsonResult.error(e.getMessage());
    }

    // 请求体不存在或格式错误
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public JsonResult handler(HttpMessageNotReadableException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "请求体解析异常", e);
        return JsonResult.error(e.getMessage());
    }

    // 上传文件超过大小限制
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public JsonResult handler(MaxUploadSizeExceededException e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "上传文件超过大小限制", e);
        return JsonResult.error(e.getMessage());
    }

    // 兜底异常：捕获所有未处理异常，避免返回 Spring 默认 500 错误页
    @ExceptionHandler(Exception.class)
    public JsonResult handler(Exception e, HttpServletRequest request) {
        RequestLogUtils.logRequestError(request, "系统异常", e);
        return JsonResult.error("系统繁忙，请稍后重试！");
    }
}
