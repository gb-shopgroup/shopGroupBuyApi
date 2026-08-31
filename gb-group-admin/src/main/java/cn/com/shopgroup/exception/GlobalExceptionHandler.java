package cn.com.shopgroup.exception;

import cn.com.shopgroup.common.utils.JsonResult;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    public Map handler(BindException e) {

        e.printStackTrace();
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return JsonResult.error(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Map handler(ConstraintViolationException e) {

        e.printStackTrace();
        String message = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining());
        return JsonResult.error(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map handler(MethodArgumentNotValidException e) {

        e.printStackTrace();
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return JsonResult.error(message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Map handler(HttpRequestMethodNotSupportedException e){

        return JsonResult.error("请求接口不存在！");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Map handler(HttpMessageNotReadableException e){

        return JsonResult.error("请求体不存在！");
    }

    // 缺少token令牌或者令牌错误
    @ExceptionHandler(TokenException.class)
    public Map handler(TokenException e) {

        return JsonResult.forbid();
    }


}
