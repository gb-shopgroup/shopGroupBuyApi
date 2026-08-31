package cn.com.shopgroup.common.utils;

import com.alibaba.fastjson2.JSON;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

// 华为云存储
public class HuaWeiOBS {

    // 记录日志
    private static final Logger log = LoggerFactory.getLogger(HuaWeiOBS.class);

    // 华为云存储的AK和SK
    private final static String AK = "HPUAIKM3OFB4TVL9ARVB";
    private final static String SK = "iBsjv2uyugEUZaCOD9Um4rRaGOK2j9SaA8SYz0j4";

    // 桶
    private final static String BucketName = "shopgroup";
    // EndPoint
    private final static String EndPoint = "https://obs.cn-north-9.myhuaweicloud.com";

    // Domain
    private final static String Domain = "https://shopgroup.obs.cn-north-9.myhuaweicloud.com";

    // 上传图片文件
    public static String upload(String key, InputStream ipts) {

        // 使用永久AK/SK初始化客户端 ObsClient 实例
        ObsClient obsClient = new ObsClient(AK, SK, EndPoint);

        try {

            // 上传本地流文件
            PutObjectRequest request = new PutObjectRequest();
            request.setBucketName(BucketName);
            request.setObjectKey(key);
            request.setInput(ipts);
            obsClient.putObject(request);

            // 返回图片访问地址
            return Domain + "/" + key;

        } catch (ObsException e) {

            // 上传失败
            log.error("putObject failed");
            // 请求失败,打印http状态码
            log.error("HTTP Code:" + e.getResponseCode());
            // 请求失败,打印服务端错误码
            log.error("Error Code:" + e.getErrorCode());
            // 请求失败,打印详细错误信息
            log.error("Error Message:" + e.getErrorMessage());
            // 请求失败,打印请求id
            log.error("Request ID:" + e.getErrorRequestId());
            log.error("Host ID:" + e.getErrorHostId());
            // 打印异常轨迹
            e.printStackTrace();
            return "";

        } catch (Exception e) {

            // 上传失败
            log.error("putObject error:{}",e);
            return "";
        }
    }

    // 删除图片文件
    public static boolean remove(String key){

        // 使用永久AK/SK初始化客户端 ObsClient 实例
        ObsClient obsClient = new ObsClient(AK, SK, EndPoint);

        try {
            obsClient.deleteObject(BucketName, key);
            return true;
        } catch (ObsException e) {

            // 上传失败
            log.error("deleteObject failed,e:{}", JSON.toJSONString(e));

            return false;

        } catch (Exception e) {
            // 上传失败
            log.error("deleteObject failed,e:{}",JSON.toJSONString(e));
            return false;
        }
    }


}
