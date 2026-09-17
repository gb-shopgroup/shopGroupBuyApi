package cn.com.shopgroup.common.wxmini;

import cn.com.shopgroup.common.response.WxOrderNotifyConfirmResponse;
import cn.com.shopgroup.common.utils.HuaWeiOBS;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WxMiniProgramHelper {

    /**
     * 微信小程序 appid; 由 WxMiniConfig 在启动时按 wx-mini.appid 覆盖(空配置保持默认值)
     */
    public static String APP_ID = "wx959de27ff559b753";

    /**
     * 微信小程序 secret; 由 WxMiniConfig 在启动时按 wx-mini.secret 覆盖(空配置保持默认值)
     */
    public static String APP_SECRET = "8dbfa901d0f7f692f9479d3b6743fb65";

    /**
     * 小程序码(getwxacodeunlimit)扫码目标版本:
     * trial=体验版, release=正式版, develop=开发版
     * 由 WxMiniConfig 在启动时按 wx-mini.env-version 覆盖, 用于不同部署环境(dev/test=体验版, prod=正式版)
     */
    public static String ENV_VERSION = "release";


    public static Map<String, String> getAccessToken() {
        return getAccessToken(false);
    }

    // 稳定版接口获取access_token(https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/getStableAccessToken.html)
    // 相比普通cgi-bin/token接口: 有效期内并发请求会返回同一个token, 避免多实例并发刷新导致token互相失效(40001 not latest)
    // forceRefresh=true时强制刷新, 会使旧token立即失效, 仅在token丢失/报40001时使用
    public static Map<String, String> getAccessToken(boolean forceRefresh) {

        Map<String, String> result = new HashMap();
        String url = "https://api.weixin.qq.com/cgi-bin/stable_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("grant_type", "client_credential");
        paramMap.put("appid", APP_ID);
        paramMap.put("secret", APP_SECRET);
        paramMap.put("force_refresh", forceRefresh);

        try {
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(paramMap, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(url, httpEntity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("access_token")) {
                result.put("success", "1");
                result.put("data", responseBody.get("access_token").toString());
            } else {
                result.put("success", "0");
                result.put("data", responseBody == null ? "响应为空" : responseBody.get("errmsg").toString());
            }
        } catch (Exception e) {
            log.error("调用微信stable_token接口异常", e);
            result.put("success", "0");
            result.put("data", e.toString());
        }
        return result;
    }


    public static Map<String, String> getPhoneNumber(String accessToken, String code) {


        Map<String, String> result = new HashMap();


        String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken;


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("code", code);
        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(paramMap, headers);


        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(url, httpEntity, Map.class);
        Map<String, Object> responseBody = response.getBody();


        String errcode = responseBody.get("errcode").toString();
        String errmsg = responseBody.get("errmsg").toString();
        int errocdeVal = Integer.parseInt(errcode);
        if (errocdeVal == 0) {

            String phoneInfo = responseBody.get("phone_info").toString();
            phoneInfo = phoneInfo.substring(1, phoneInfo.length() - 1);
            String[] phoneInfoArray = phoneInfo.split(", ");
            Map<String, String> phoneInfoMap = new HashMap<>();
            for (String pair : phoneInfoArray) {
                String[] keyValue = pair.split("=");
                phoneInfoMap.put(keyValue[0], keyValue[1]);
            }

            if (phoneInfoMap.containsKey("phoneNumber")) {
                String phoneNumber = phoneInfoMap.get("phoneNumber");
                result.put("success", "1");
                result.put("data", phoneNumber);
            } else {
                result.put("success", "0");
                result.put("data", "phoneNumber is null");
            }
        } else {

            if (errocdeVal == 40001 || errocdeVal == 42001) {
                result.put("success", "0");
                result.put("data", "access_token");
            } else {
                result.put("success", "0");
                result.put("data", errmsg);
            }
        }
        return result;
    }


    public static Map<String, String> getOpenId(String code) {

        Map<String, String> result = new HashMap();
        String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code", APP_ID, APP_SECRET, code);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        String responseBody = response.getBody().toString();
        JSONObject json = JSON.parseObject(responseBody);
        String openid = json.getString("openid");

        if (openid != null && openid.length() > 0) {
            result.put("success", "1");
            result.put("data", openid);
        } else {
            result.put("success", "0");
            result.put("data", "openid is null");
        }
        return result;
    }


    /**
     * 小程序码(getwxacodeunlimit)生成结果
     * imageBytes非空表示成功; 否则errcode/errmsg为微信返回的失败原因
     */
    public static class WxQrCodeResult {

        /**
         * 成功时的小程序码图片字节, 失败时为null
         */
        private final byte[] imageBytes;
        /**
         * 失败时微信返回的错误码
         */
        private final Integer errcode;
        /**
         * 失败时微信返回的错误信息
         */
        private final String errmsg;

        public WxQrCodeResult(byte[] imageBytes, Integer errcode, String errmsg) {
            this.imageBytes = imageBytes;
            this.errcode = errcode;
            this.errmsg = errmsg;
        }

        public boolean isOk() {
            return imageBytes != null && imageBytes.length > 0;
        }

        /**
         * 是否access_token失效(40001 invalid credential / 42001 expired)
         * 可强刷token后重试
         */
        public boolean isTokenInvalid() {
            return errcode != null && (errcode == 40001 || errcode == 42001);
        }

        public byte[] getImageBytes() {
            return imageBytes;
        }

        public Integer getErrcode() {
            return errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }
    }

    /**
     * 调用微信小程序码接口(getwxacodeunlimit)
     * 成功返回图片字节, 失败返回带errcode/errmsg的结果
     * 常见错误: 40001 token失效 / 41030 page不存在(未发布) / 40097 scene非法(超32字符)
     */
    public static WxQrCodeResult getWxaCodeUnlimit(String accessToken, String page, String scene, int wh) {

        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken;

        JSONObject params = new JSONObject();
        params.put("page", page);
        params.put("scene", scene);
        params.put("width", wh);
        params.put("auto_color", false);
        params.put("is_hyaline", false);
        // 扫码目标版本: 由 WxMiniConfig 按 wx-mini.env-version 覆盖(默认 release)
        params.put("env_version", ENV_VERSION);
        log.info("调用微信生成小程序码参数parars:{}", JSON.toJSONString(params));
        HttpURLConnection connection = null;
        try {
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setUseCaches(false);

            PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
            printWriter.write(params.toJSONString());
            printWriter.flush();

            // 读取完整响应: 微信成功时返回图片字节, 失败时返回JSON错误(HTTP错误流)
            InputStream is = connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream();
            byte[] bytes = readAll(is);
            if (bytes == null || bytes.length == 0) {
                return new WxQrCodeResult(null, -1, "微信返回空响应");
            }
            // 通过图片魔数(PNG/JPEG)判定, 避免把JSON错误当图片解析成null
            boolean isImage = ((bytes[0] & 0xFF) == 0x89 && (bytes[1] & 0xFF) == 0x50)
                    || ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8);
            if (isImage) {
                return new WxQrCodeResult(bytes, 0, null);
            }
            // 失败: 解析微信错误码, 便于定位真实原因
            try {
                JSONObject err = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8));
                return new WxQrCodeResult(null, err.getInteger("errcode"), err.getString("errmsg"));
            } catch (Exception e) {
                return new WxQrCodeResult(null, -1, new String(bytes, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            log.error("调用微信小程序码接口异常 page:{} scene:{}", page, scene, e);
            return new WxQrCodeResult(null, -1, e.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static byte[] readAll(InputStream is) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }

    public static BufferedImage getMiniProgramPageERcodeBufferedImage(String accessToken, String page, String scene, int wh) {

        WxQrCodeResult result = getWxaCodeUnlimit(accessToken, page, scene, wh);
        if (!result.isOk()) {
            log.error("获取微信小程序码失败 page:{} scene:{} errcode:{} errmsg:{}",
                    page, scene, result.getErrcode(), result.getErrmsg());
            return null;
        }
        try {
            return ImageIO.read(new ByteArrayInputStream(result.getImageBytes()));
        } catch (IOException e) {
            log.error("解析微信小程序码图片失败", e);
            return null;
        }
    }


    public static boolean getMiniProgramPageERcode(String accessToken, String page, String scene, String filePath, int wh, int type) {

        WxQrCodeResult result = getWxaCodeUnlimit(accessToken, page, scene, wh);
        if (!result.isOk()) {
            log.error("获取微信小程序码失败 page:{} scene:{} errcode:{} errmsg:{}",
                    page, scene, result.getErrcode(), result.getErrmsg());
            return false;
        }
        try {
            InputStream is = new ByteArrayInputStream(result.getImageBytes());
            if (type == 1) {
                saveLocalFile(filePath, is);
            } else {
                HuaWeiOBS.upload(filePath, is);
            }
            return true;
        } catch (Exception e) {
            log.error("保存微信小程序码失败 filePath:{}", filePath, e);
            return false;
        }
    }


    private static String httpGet(String url) {

        HttpURLConnection connection = null;
        try {


            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);


            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return "";
    }


    private static void httpPostForImage(String url, String params, String filePath, int type) {

        HttpURLConnection connection = null;
        try {


            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setUseCaches(false);


            PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
            printWriter.write(params);
            printWriter.flush();


            if (type == 1) {

                saveLocalFile(filePath, connection.getInputStream());
            } else {

                HuaWeiOBS.upload(filePath, connection.getInputStream());
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.info("获取微信小程序二维码失败：" + e.toString());
        } finally {
            connection.disconnect();
        }
    }

    //发货信息录入
    public static int uploadShippingInfo(String accessToken, String transactionId, String goodsName, String openid) {


        String url = "https://api.weixin.qq.com/wxa/sec/order/upload_shipping_info?access_token=" + accessToken;


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        WxSendGoodsRequest data = new WxSendGoodsRequest(transactionId, goodsName, openid);
        String json = JSON.toJSONString(data);

        RestTemplate restTemplate = new RestTemplate();
        log.info("微信发货参数data:{}" + JSON.toJSONString(data));
        ResponseEntity<Map> response = restTemplate.postForEntity(url, data, Map.class);
        log.info("微信发货响应：" + response.getBody().toString());
        Map<String, Object> responseBody = response.getBody();

        String errcode = responseBody.get("errcode").toString();
        String errmsg = responseBody.get("errmsg").toString();
        int errocdeVal = Integer.parseInt(errcode);
        if (errocdeVal == 0 && errmsg.equalsIgnoreCase("ok")) {
            return 1;
        } else {
            if (errocdeVal == 40001 || errocdeVal == 42001) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    //查询订单发货状态
    public static int getWxOrder(String accessToken, String transactionId) {

        String url = "https://api.weixin.qq.com/wxa/sec/order/get_order?access_token=" + accessToken;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        WxOrderQuery data = new WxOrderQuery(transactionId);
        String json = JSON.toJSONString(data);


        RestTemplate restTemplate = new RestTemplate();
        log.info("查询订单发货状态参数:data:{}", JSON.toJSONString(data));
        ResponseEntity<Map> response = restTemplate.postForEntity(url, data, Map.class);
        log.info("微信查询订单响应：" + response.getBody().toString());
        Map<String, Object> responseBody = response.getBody();
        String errcode = responseBody.get("errcode").toString();
        String errmsg = responseBody.get("errmsg").toString();
        int errocdeVal = Integer.parseInt(errcode);
        if (errocdeVal == 0 && errmsg.equalsIgnoreCase("ok")) {

            String content = responseBody.get("order").toString();
            Pattern pattern = Pattern.compile("order_state=(\\d+)");
            Matcher matcher = pattern.matcher(content);
            Integer orderState = null;
            if (matcher.find()) {
                orderState = Integer.parseInt(matcher.group(1));
            }
            return orderState;
        } else {
            if (errocdeVal == 40001 || errocdeVal == 42001) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    //确认收货提醒
    public static WxOrderNotifyConfirmResponse notifyConfirmReceive(String accessToken, String transactionId) {

        String url = "https://api.weixin.qq.com/wxa/sec/order/notify_confirm_receive?access_token=" + accessToken;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        WxOrderQuery data = new WxOrderQuery(transactionId);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(url, data, Map.class);
        log.info("确认收货提醒响应：" + response.getBody().toString());
        Map<String, Object> responseBody = response.getBody();
        String errcode = responseBody.get("errcode").toString();
        String errmsg = responseBody.get("errmsg").toString();
        int errocdeVal = Integer.parseInt(errcode);
        WxOrderNotifyConfirmResponse resp = new WxOrderNotifyConfirmResponse();
        resp.setCode(errocdeVal);
        resp.setMsg(errmsg);
        return resp;
    }


    public static void saveLocalFile(String filePath, InputStream inputStream) throws IOException {


        OutputStream os = new FileOutputStream(new File(filePath));
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        int len;
        byte[] arr = new byte[1024];
        while ((len = bis.read(arr)) != -1) {
            os.write(arr, 0, len);
            os.flush();
        }
        os.close();
    }


}