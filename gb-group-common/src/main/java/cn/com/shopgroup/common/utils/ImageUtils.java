package cn.com.shopgroup.common.utils;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ImageUtils {

    // 单文件最大 5MB 字节数
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // 允许的图片后缀
    private static final Set<String> ALLOW_SUFFIX = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp"));

    // 允许的图片MIME类型（防篡改后缀）
    private static final Set<String> ALLOW_MIME = new HashSet<>(Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp"));

    // 统一文件校验：大小 + 图片类型
    public static void validateFile(MultipartFile file) {

        // 1. 校验文件大小
        long fileSize = file.getSize();
        if (fileSize > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过5MB");
        }
        if (fileSize <= 0) {
            throw new RuntimeException("文件不能为空");
        }

        // 2. 校验MIME类型（优先，防改后缀）
        String contentType = file.getContentType();
        if (contentType == null || !ALLOW_MIME.contains(contentType.toLowerCase())) {
            throw new RuntimeException("仅支持 jpg/png/gif/bmp 格式图片");
        }

        // 3. 校验文件后缀（双重兜底）
        String fileName = file.getOriginalFilename();
        String suffix = getFileSuffix(fileName).toLowerCase();
        if (!ALLOW_SUFFIX.contains(suffix)) {
            throw new RuntimeException("仅支持 jpg/png/gif/bmp 格式图片");
        }
    }

    // 截取文件后缀
    public static String getFileSuffix(String fileName) {

        if (fileName == null || !fileName.contains(".")) { return ""; }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    // 原图存储到本地(区分私有文件和共有文件)
    public static String saveLocalFile(MultipartFile file, String domain, String rootPath){

        // 生成新文件名称
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + suffix;

        // 生成日期路径
        String datePath = TimeUtils.getTodayStr();
        File fileFolder = new File(rootPath + File.separator + datePath);
        if (!fileFolder.exists()) fileFolder.mkdirs();

        // 保存图片文件
        try {
            File uploadedFile = new File(fileFolder, newFileName);
            file.transferTo(uploadedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 返回数据
        return domain + "/" + datePath + "/" + newFileName;
    }

    // 读取图片宽高，返回 [宽, 高]
    public static int[] getImageSize(MultipartFile file) {

        try {
            byte[] fileBytes = file.getBytes();
            InputStream is = new ByteArrayInputStream(fileBytes);
            BufferedImage image = ImageIO.read(is);
            if(image == null){
                return new int[]{0, 0};
            }else{
                return new int[]{image.getWidth(), image.getHeight()};
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return new int[]{0, 0};
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


}
