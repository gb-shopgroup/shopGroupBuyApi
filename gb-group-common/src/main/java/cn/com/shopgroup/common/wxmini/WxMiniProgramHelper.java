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

    private final static String AppID = "wx959de27ff559b753";
    private final static String AppSecret = "8dbfa901d0f7f692f9479d3b6743fb65";


    public static Map<String, String> getAccessToken() {

        Map<String, String> result = new HashMap();
        String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", AppID, AppSecret);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> responseBody = response.getBody();


        if (responseBody.containsKey("access_token")) {
            result.put("success", "1");
            result.put("data", responseBody.get("access_token").toString());
        } else {
            result.put("success", "0");
            result.put("data", responseBody.get("errmsg").toString());
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
        String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code", AppID, AppSecret, code);
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


    public static BufferedImage getMiniProgramPageERcodeBufferedImage(String accessToken, String page, String scene, int wh) {


        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken;


        JSONObject params = new JSONObject();
        params.put("page", page);
        params.put("scene", scene);
        params.put("width", wh);
        params.put("auto_color", false);
        params.put("is_hyaline", false);
        params.put("env_version", "release");


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


            return ImageIO.read(connection.getInputStream());

        } catch (Exception e) {
            e.printStackTrace();
            log.info("获取微信小程序二维码失败：" + e.toString());
        } finally {
            connection.disconnect();
        }

        return null;
    }


    public static boolean getMiniProgramPageERcode(String accessToken, String page, String scene, String filePath, int wh, int type) {


        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken;


        JSONObject params = new JSONObject();
        params.put("page", page);
        params.put("scene", scene);
        params.put("width", wh);
        params.put("auto_color", false);
        params.put("is_hyaline", false);
        params.put("env_version", "release");


        httpPostForImage(url, params.toJSONString(), filePath, type);


        return true;
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