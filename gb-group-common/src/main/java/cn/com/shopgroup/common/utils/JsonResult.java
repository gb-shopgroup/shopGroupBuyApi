package cn.com.shopgroup.common.utils;

import java.util.HashMap;

// 统一返回数据结构
public class JsonResult extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    // 状态码：成功
    public static final int SUCCESS_CODE = 200;
    // 状态码：失败
    public static final int FAIL_CODE = 300;
    // 状态码：无权限
    public static final int AUTH_CODE = 400;
    // 状态码：错误或异常
    public static final int ERROR_CODE = 500;

    // 状态码
    public static final String CODE_TAG = "code";
    // 提示信息
    public static final String MSG_TAG = "msg";
    // 数据对象
    public static final String DATA_TAG = "data";

    // 默认构造方法
    public JsonResult() {}

    // 有参构造方法
    public JsonResult(int code, String msg) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    // 有参构造方法
    public JsonResult(int code, String msg, Object data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        super.put(DATA_TAG, data);
    }

    // 返回成功消息
    public static JsonResult success() {
        return new JsonResult(SUCCESS_CODE, "操作成功", null);
    }

    // 返回成功消息
    public static JsonResult success(String msg) {
        return new JsonResult(SUCCESS_CODE, msg, null);
    }

    // 返回成功消息
    public static JsonResult success(Object data) {
        return new JsonResult(SUCCESS_CODE, "操作成功", data);
    }

    // 返回成功消息
    public static JsonResult success(String msg, Object data) {
        return new JsonResult(SUCCESS_CODE, msg, data);
    }

    // 返回失败消息
    public static JsonResult fail() {
        return new JsonResult(FAIL_CODE, "操作失败", null);
    }

    // 返回失败消息
    public static JsonResult fail(String msg) {
        return new JsonResult(FAIL_CODE, msg, null);
    }

    // 返回错误消息
    public static JsonResult error(String msg) {
        return new JsonResult(ERROR_CODE, msg, null);
    }

    // 返回无Token权限消息
    public static JsonResult forbid() {
        return new JsonResult(AUTH_CODE, "没有权限", null);
    }

}