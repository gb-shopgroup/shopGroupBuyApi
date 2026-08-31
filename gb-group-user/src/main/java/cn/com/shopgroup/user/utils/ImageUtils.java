package cn.com.shopgroup.user.utils;

import cn.com.shopgroup.common.utils.HuaWeiOBS;
import cn.com.shopgroup.common.utils.TimeUtils;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ImageUtils {

    /**
     * 图片压缩为 400*400 方形（等比例缩放+居中裁剪，适配所有原图尺寸）
     * @param file 上传文件
     * @return OBS 公开访问URL
     */
    public static String uploadAndCrop(MultipartFile file, int TARGET_WIDTH, int TARGET_HEIGHT, String path) {

        try {
            // 压缩裁剪图片，输出到字节流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    // 固定宽高 + 居中裁剪，保证最终 400*400
                    .size(TARGET_WIDTH, TARGET_HEIGHT)
                    // 开启裁剪（核心）
                    .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
                    // 图片质量 0.0~1.0，兼顾体积和清晰度
                    .outputQuality(1.0f)
                    .toOutputStream(outputStream);

            // 字节流转输入流，用于OBS上传
            InputStream compressStream = new ByteArrayInputStream(outputStream.toByteArray());

            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String suffix = getFileSuffix(originalName);
            String fileName = UUID.randomUUID() + suffix;
            String obsKey = path + "/" + TimeUtils.getTodayStr() + "/" + fileName;

            // 上传到华为云OBS并返回访问地址
            return HuaWeiOBS.upload(obsKey, compressStream);

        } catch (IOException e) {

            throw new RuntimeException("图片压缩/上传失败", e);
        }
    }

    /**
     * 等比例缩放，宽高均不超过400px，保留原图比例，不裁剪
     * @param file 上传文件
     * @return OBS 公开访问URL
     */
    public static String uploadAndScale(MultipartFile file, int TARGET_WIDTH, int TARGET_HEIGHT, String path) {

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(TARGET_WIDTH, TARGET_HEIGHT)
                    .outputQuality(1.0f)
                    .toOutputStream(outputStream);
            InputStream compressStream = new ByteArrayInputStream(outputStream.toByteArray());
            String suffix = getFileSuffix(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + suffix;
            String obsKey = path + "/" + TimeUtils.getTodayStr() + "/" + fileName;
            return HuaWeiOBS.upload(obsKey, compressStream);
        } catch (IOException e) {
            throw new RuntimeException("图片压缩/上传失败", e);
        }
    }

    // 直接上传云存储
    public static String uploadFile(MultipartFile file, String path){

        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e){
            e.printStackTrace();
        }
        if(inputStream == null) return "";

        String suffix = getFileSuffix(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + suffix;
        String obsKey = path + "/" + TimeUtils.getTodayStr() + "/" + fileName;
        return HuaWeiOBS.upload(obsKey, inputStream);
    }


    // 截取文件后缀（兼容无后缀、多分隔符文件名）
    private static String getFileSuffix(String fileName) {

        if (fileName == null || !fileName.contains(".")) { return ".jpg"; }
        return fileName.substring(fileName.lastIndexOf("."));
    }


}
