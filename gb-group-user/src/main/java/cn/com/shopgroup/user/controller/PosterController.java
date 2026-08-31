package cn.com.shopgroup.user.controller;

import cn.com.shopgroup.common.utils.PosterDTO;
import cn.com.shopgroup.common.utils.PosterGroupUtils;
import cn.com.shopgroup.common.utils.PosterParam;
import cn.com.shopgroup.common.utils.PosterUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping("/user")
public class PosterController {

//    @GetMapping("/image/upload/poster")
//    public void poster(HttpServletResponse response) throws Exception {
//
//        // 海报信息
//        PosterDTO dto = new PosterDTO();
//        dto.setGoodsName("康师傅红烧牛肉面");
//        dto.setSaleText("已售200单");
//        dto.setPriceText("￥19.90");
//        dto.setBtnText("立即跟团");
//        dto.setImgUrl("https://shopgroup.obs.cn-north-9.myhuaweicloud.com/goods/20260630/2af33f86-7240-4379-ba25-4089ee7ef174.jpg");
//
//        // 生成海报
//        ByteArrayInputStream bis = PosterUtils.generatePosterStream(dto);
//
//        // 设置返回图片响应头
//        response.setContentType("image/png");
//        try (OutputStream os = response.getOutputStream()) {
//            byte[] buf = new byte[4096];
//            int len;
//            while ((len = bis.read(buf)) != -1) {
//                os.write(buf, 0, len);
//            }
//        }
//        bis.close();
//    }
//
//    @GetMapping("/image/upload/poster2")
//    public void poster2(HttpServletResponse response) throws Exception {
//
//        // 参数
//        PosterParam param = new PosterParam();
//        param.shopLogoUrl = "http://127.0.0.1:9003/image/file/head01.png"; //替换你的logo网络地址
//        param.shopName = "朝霞烟酒店";
//        param.showTime = "07/07 08:35:45";
//        param.hotImgLocalPath = new FileInputStream("D:/poster.png"); //本地爆款推荐图片
//        param.goodsImgUrl = "http://127.0.0.1:9003/image/file/goods03.png"; //商品网络图片
//        param.goodsName = "精品飞天茅台500ml原装正品";
//        param.price = "￥2680元";
//        param.sales = "已售132件";
//        param.ercodeImage = fileToBufferedImage("E:/idea_workspace/GroupBuyApi/image/public/ercode.png"); //本地二维码图片
//        param.tipText = "长按识别,跟团购买";
//
//        // 生成海报
//        ByteArrayInputStream bis = PosterGroupUtils.generatePoster(param);
//
//        // 设置返回图片响应头
//        response.setContentType("image/png");
//        try (OutputStream os = response.getOutputStream()) {
//            byte[] buf = new byte[4096];
//            int len;
//            while ((len = bis.read(buf)) != -1) {
//                os.write(buf, 0, len);
//            }
//        }
//        bis.close();
//    }
//
//    // 本地文件转 BufferedImage
//    private static BufferedImage fileToBufferedImage(String filePath) throws IOException {
//        File file = new File(filePath);
//        return ImageIO.read(file);
//    }
//
//    @PostMapping("/image/upload/goods/poster")
//    public void goodsPoster(@RequestBody PosterParam param, HttpServletResponse response) throws Exception {
//        // 参数
//        // 生成海报
//        ByteArrayInputStream bis = PosterGroupUtils.generatePoster(param);
//
//        // 设置返回图片响应头
//        response.setContentType("image/png");
//        try (OutputStream os = response.getOutputStream()) {
//            byte[] buf = new byte[4096];
//            int len;
//            while ((len = bis.read(buf)) != -1) {
//                os.write(buf, 0, len);
//            }
//        }
//        bis.close();
//    }

}
