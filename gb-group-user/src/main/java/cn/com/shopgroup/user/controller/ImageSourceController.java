package cn.com.shopgroup.user.controller;

import cn.com.shopgroup.common.config.UploadConfig;
import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.user.exception.UserErrorCodeEnum;
import cn.com.shopgroup.user.service.GbImageLibraryInfoService;
import cn.com.shopgroup.user.utils.ImageUtils;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class ImageSourceController {

    @Resource
    private UploadConfig uploadConfig;

    @Resource
    private GbImageLibraryInfoService service;

    // 单文件最大 5MB 字节数
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // 允许的图片后缀
    private static final Set<String> ALLOW_SUFFIX = new HashSet<>(
            Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp")
    );

    // 允许的图片MIME类型（防篡改后缀）
    private static final Set<String> ALLOW_MIME = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp")
    );

    // 统一文件校验：大小 + 图片类型
    private void validateFile(MultipartFile file) {

        // 1. 校验文件大小
        long fileSize = file.getSize();
        if (fileSize > MAX_FILE_SIZE) {
            throw new BusinessException(UserErrorCodeEnum.FILE_SIZE_EXCEEDED);
        }
        if (fileSize <= 0) {
            throw new BusinessException(UserErrorCodeEnum.FILE_EMPTY);
        }

        // 2. 校验MIME类型（优先，防改后缀）
        String contentType = file.getContentType();
        if (contentType == null || !ALLOW_MIME.contains(contentType.toLowerCase())) {
            throw new BusinessException(UserErrorCodeEnum.IMAGE_FORMAT_NOT_SUPPORTED);
        }

        // 3. 校验文件后缀（双重兜底）
        String fileName = file.getOriginalFilename();
        String suffix = getFileSuffix(fileName).toLowerCase();
        if (!ALLOW_SUFFIX.contains(suffix)) {
            throw new BusinessException(UserErrorCodeEnum.IMAGE_FORMAT_NOT_SUPPORTED);
        }
    }

    // 截取文件后缀
    private String getFileSuffix(String fileName) {

        if (fileName == null || !fileName.contains(".")) { return ""; }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    // 原图存储到本地(区分私有文件和共有文件)
    private String saveLocalFile(MultipartFile file, boolean isPrivate){

        // 访问域名和存储路径
        String domain = "";
        String rootPath = "";
        if(isPrivate){
            // 私有
            domain = uploadConfig.getDomain2();
            rootPath = uploadConfig.getPath2();
        } else {
            // 共有
            domain = uploadConfig.getDomain();
            rootPath = uploadConfig.getPath();
        }

        // 生成新文件名称
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + suffix;

        // 生成日期路径
        String datePath = TimeUtils.getTodayStr();
        File fileFolder = new File(rootPath + File.separator + datePath);
        if (!fileFolder.exists() && !fileFolder.mkdirs()) {
            throw new BusinessException(UserErrorCodeEnum.DIRECTORY_CREATE_FAILED);
        }

        // 保存图片文件
        try {
            File uploadedFile = new File(fileFolder, newFileName);
            file.transferTo(uploadedFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(UserErrorCodeEnum.FILE_SAVE_FAILED);
        }

        // 返回数据
        return domain + "/" + datePath + "/" + newFileName;
    }

    // 读取图片宽高，返回 [宽, 高]
    private int[] getImageSize(MultipartFile file) {

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

    // 上传用户头像
    @PostMapping("/image/upload/avatar")
    public JsonResult uploadAvatar(@RequestParam("file") MultipartFile file) {

        // 返回图片访问路径
        String url = "";
        String path = "avatar";

        // 图片不能为空
        if (file.isEmpty()) { throw new BusinessException(UserErrorCodeEnum.FILE_EMPTY); }

        // 执行校验
        validateFile(file);

        // 判断本地上传还是云存储上传: # 1=本地上传, 2=云端上传
        int type = uploadConfig.getType();
        if(type == 1){
            // 上传到本地
            url = this.saveLocalFile(file, false);
        }else{
            // 直接上传华为云存储
            url = ImageUtils.uploadFile(file, path);
        }

        // 存储到数据库里面
        if(url.length() > 0){
            service.addImage(0L, (byte)0, url, (byte)0);
            return JsonResult.success("上传成功", url);
        }else{
            throw new BusinessException(UserErrorCodeEnum.IMAGE_UPLOAD_FAILED);
        }
    }

    // 上传商品图片(尺寸400*400)
    @PostMapping("/image/upload/goods")
    public JsonResult uploadGoods(@RequestParam("file") MultipartFile file) {

        // 返回图片访问路径
        String url = "";

        // 商品图片大小
        int width = 400;
        int height = 400;
        String path = "goods";

        // 图片不能为空
        if (file.isEmpty()) { throw new BusinessException(UserErrorCodeEnum.FILE_EMPTY); }

        // 执行校验
        validateFile(file);

        // 获取图片尺寸, 是否压缩
        int[] size = this.getImageSize(file);
        int imgWidth = size[0];
        int imgHeight = size[1];
        if(imgWidth == 0 || imgHeight == 0){ throw new BusinessException(UserErrorCodeEnum.IMAGE_SIZE_INVALID); }
        boolean isZip = true;
        if (imgWidth == width && imgHeight == height) { isZip = false; }

        // 判断本地上传还是云存储上传: # 1=本地上传, 2=云端上传
        int type = uploadConfig.getType();
        if(type == 1){
            // 上传到本地
            url = this.saveLocalFile(file, false);
        }else{
            if(isZip){
                // 等比例缩放+居中裁剪
                url = ImageUtils.uploadAndCrop(file, width, height, path);
            }else{
                // 不需要压缩
                url = ImageUtils.uploadFile(file, path);
            }
        }

        // 存储到数据库里面
        if(url.length() > 0){
            service.addImage(0L, (byte)0, url, (byte)0);
            return JsonResult.success("上传成功", url);
        }else{
            throw new BusinessException(UserErrorCodeEnum.IMAGE_UPLOAD_FAILED);
        }
    }

    // 上传店铺banner(尺寸750*250)
    @PostMapping("/image/upload/banner")
    public JsonResult uploadBanner(@RequestParam("file") MultipartFile file){

        // 返回图片访问路径
        String url = "";

        // 商品图片大小
        int width = 750;
        int height = 250;
        String path = "banner";

        // 图片不能为空
        if (file.isEmpty()) { throw new BusinessException(UserErrorCodeEnum.FILE_EMPTY); }

        // 执行校验
        validateFile(file);

        // 获取图片尺寸, 是否压缩
        int[] size = this.getImageSize(file);
        int imgWidth = size[0];
        int imgHeight = size[1];
        if(imgWidth == 0 || imgHeight == 0){ throw new BusinessException(UserErrorCodeEnum.IMAGE_SIZE_INVALID); }
        boolean isZip = true;
        if (imgWidth == width && imgHeight == height) { isZip = false; }

        // 判断本地上传还是云存储上传: # 1=本地上传, 2=云端上传
        int type = uploadConfig.getType();
        if(type == 1){
            // 上传到本地
            url = this.saveLocalFile(file, false);
        }else{
            if(isZip){
                // 等比例缩放+居中裁剪
                //url = ImageUtils.uploadAndCrop(file, width, height, path);
                // 保留原图比例，不裁剪
                url = ImageUtils.uploadAndScale(file, width, height, path);
            }else{
                // 不需要压缩
                url = ImageUtils.uploadFile(file, path);
            }
        }

        // 存储到数据库里面
        if(url.length() > 0){
            service.addImage(0L, (byte)0, url, (byte)0);
            return JsonResult.success("上传成功", url);
        }else{
            throw new BusinessException(UserErrorCodeEnum.IMAGE_UPLOAD_FAILED);
        }
    }


    // 查看私有图片
    @GetMapping("/image/access/{*file}")
    public JsonResult access(@PathVariable("file") String file){

        // 返回结果
        String message = "查询成功";

        // 读取图片文件
        byte[] imageBytes = null;
        File img = new File(uploadConfig.getPath2() + File.separator + file);
        try {
            imageBytes = Files.readAllBytes(img.toPath());
        }catch (IOException e){
            e.printStackTrace();
            throw new BusinessException(UserErrorCodeEnum.IMAGE_READ_FAILED);
        }

        // 将图片字节数组进行 Base64 编码
        String base64EncodedImage = Base64Utils.encodeToString(imageBytes);
        String body = "data:image/jpeg;base64," + base64EncodedImage;

        // 返回结果
        return JsonResult.success(message, body);
    }

}
