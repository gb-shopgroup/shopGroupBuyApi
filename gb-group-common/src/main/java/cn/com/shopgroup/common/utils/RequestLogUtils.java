package cn.com.shopgroup.common.utils;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请求日志工具类
 * 在全局异常处理等场景打印请求方式、URI 与请求参数，便于快速定位问题。
 * 参数包含 Query 参数与表单参数（application/x-www-form-urlencoded）；
 * 对 JSON 请求体（@RequestBody）参数，因输入流已被读取无法重复消费，可通过
 * 接口出参 / 业务日志另行排查。
 */
@Slf4j
public class RequestLogUtils {

    /** 单个参数值的最大打印长度，防止大字段刷爆日志 */
    private static final int MAX_VALUE_LEN = 500;

    private RequestLogUtils() {
    }

    /**
     * 打印请求异常信息：请求方式 + URI + Content-Type + Query 参数 + 表单参数
     *
     * @param request   当前请求（可为 null）
     * @param scene     场景说明（如"参数绑定异常"）
     * @param throwable 异常对象（可为 null）
     */
    public static void logRequestError(HttpServletRequest request, String scene, Throwable throwable) {
        try {
            if (request == null) {
                log.error("[请求异常] 场景={}, 未获取到 HttpServletRequest", scene, throwable);
                return;
            }
            String query = request.getQueryString();
            log.error("[请求异常] 场景={} 方式={} URI={} Content-Type={} Query={} 参数={}",
                    scene,
                    request.getMethod(),
                    request.getRequestURI(),
                    defaultStr(request.getContentType()),
                    query == null ? "" : query,
                    buildParams(request.getParameterMap()),
                    throwable);
        } catch (Exception ex) {
            // 日志记录本身失败时不能影响异常处理主流程
            log.error("记录请求日志失败", ex);
        }
    }

    /**
     * 将参数 Map 拼接为可读 JSON 字符串，单值超长截断
     */
    private static String buildParams(Map<String, String[]> parameterMap) {
        if (parameterMap == null || parameterMap.isEmpty()) {
            return "";
        }
        Map<String, String> flat = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] values = entry.getValue();
            String value = values == null ? "" : String.join(",", values);
            if (value.length() > MAX_VALUE_LEN) {
                value = value.substring(0, MAX_VALUE_LEN) + "...(已截断)";
            }
            flat.put(entry.getKey(), value);
        }
        return JSON.toJSONString(flat);
    }

    private static String defaultStr(String s) {
        return s == null ? "" : s;
    }
}
