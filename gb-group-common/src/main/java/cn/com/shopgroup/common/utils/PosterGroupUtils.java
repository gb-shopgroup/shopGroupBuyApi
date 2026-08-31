package cn.com.shopgroup.common.utils;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class PosterGroupUtils {

    // 全局常量
    private static final int POSTER_WIDTH = 440;
    private static final int POSTER_HEIGHT = 940;
    private static final int PADDING = 20;
    private static final Color BG_COLOR = Color.WHITE;

    // 生成海报，返回 ByteArrayInputStream
    public static ByteArrayInputStream generatePoster(PosterParam param) throws IOException {

        // 海报尺寸 440*1000
        BufferedImage posterImage = new BufferedImage(POSTER_WIDTH, POSTER_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = posterImage.createGraphics();

        // 开启抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(BG_COLOR);
        g2d.fillRect(0, 0, POSTER_WIDTH, POSTER_HEIGHT);

        // 横纵坐标值
        int contentStartX = PADDING;
        int currentY = PADDING;

        //====================第一部分：店铺标识 高100像素====================
        // 下载网络图片缩放到100*100正方形
        BufferedImage squareLogo = resizeNetImage(param.shopLogoUrl, 80, 80);
        // 将正方形图片转为圆形图片
        BufferedImage roundLogo = convertToCircleImage(squareLogo);
        g2d.drawImage(roundLogo, contentStartX, currentY, null);

        // 文字区域在logo右侧，整体相对于100px高度垂直居中
        int textAreaX = contentStartX + 90;
        Font shopNameFont = getOsAutoFont(Font.BOLD, 22);
        // 整个logo高度100，文字整体居中：中间位置大约50
        int midY = currentY + 50;
        g2d.setFont(shopNameFont);
        Color color = Color.decode("#272727");
        g2d.setColor(color);
        g2d.drawString(param.shopName, textAreaX, midY - 12);

        // 绘制时间
        Font timeFont = getOsAutoFont(Font.PLAIN, 16);
        g2d.setFont(timeFont);
        Color color2 = Color.decode("#9b9b9b");
        g2d.setColor(color2);
        g2d.drawString(param.showTime, textAreaX, midY + 18);
        currentY += 100;

        //====================第二部分：爆款推荐图片 ====================
        BufferedImage hotImage = Thumbnails.of(param.hotImgLocalPath).width(POSTER_WIDTH).asBufferedImage();
        g2d.drawImage(hotImage, contentStartX, currentY, null);
        currentY += 70;

        //====================第三部分：商品图片400*400====================
        BufferedImage goodsImage = resizeNetImage(param.goodsImgUrl, 400, 400);
        g2d.drawImage(goodsImage, contentStartX, currentY, null);
        currentY += 400;

        //====================第四部分：商品名称左对齐====================
        Font goodsNameFont = getOsAutoFont(Font.BOLD, 26);
        g2d.setFont(goodsNameFont);
        g2d.setColor(Color.BLACK);
        g2d.drawString(param.goodsName, contentStartX, currentY + 40);
        currentY += 80;

        //====================第五部分：左侧价格销量、右侧二维码垂直对齐====================

        // 绘制价格, 价格红色字体
        int centerY = currentY + 150;
        Font priceFont = getOsAutoFont(Font.BOLD, 28);
        g2d.setFont(priceFont);
        g2d.setColor(Color.RED);
        g2d.drawString(param.price, contentStartX, centerY - 15);

        // 绘制销量
        Font salesFont = getOsAutoFont(Font.PLAIN, 24);
        g2d.setFont(salesFont);
        g2d.setColor(color);
        g2d.drawString(param.sales, contentStartX+3, centerY + 25);

        // 绘制二维码图片200*200
        int qrX = contentStartX + 200;
        BufferedImage qrImg = Thumbnails.of(param.ercodeImage).size(200, 200).asBufferedImage();
        g2d.drawImage(qrImg, qrX, currentY, null);

        // 二维码下方提示文字
        Font tipFont = getOsAutoFont(Font.PLAIN, 16);
        g2d.setFont(tipFont);
        g2d.setColor(color);
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int tipWidth = fontMetrics.stringWidth(param.tipText);
        int tipX = qrX + (200 - tipWidth) / 2;
        g2d.drawString(param.tipText, tipX, currentY + 220);

        // 销毁GD
        g2d.dispose();

        // 图片转字节流返回
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(posterImage, "png", bos);
        return new ByteArrayInputStream(bos.toByteArray());
    }

    // 网络图片缩放，居中裁剪得到正方形图片，不会拉伸
    private static BufferedImage resizeNetImage(String imageUrl, int targetW, int targetH) throws IOException {

        return Thumbnails.of(new URL(imageUrl)).size(targetW, targetH).crop(Positions.CENTER).asBufferedImage();
    }

    // 将正方形BufferedImage转为圆形图片
    private static BufferedImage convertToCircleImage(BufferedImage source) {

        int size = source.getWidth();
        BufferedImage circleImage = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = circleImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //设置圆形裁剪区域
        Ellipse2D ellipse = new Ellipse2D.Double(0, 0, size, size);
        g2.setClip(ellipse);
        g2.drawImage(source, 0, 0, null);
        g2.dispose();
        return circleImage;
    }

    /**
     * 根据系统自动选择字体
     * Windows：微软雅黑
     * Linux：WenQuanyi Micro Hei 文泉驿微米黑
     * @param style 字体样式 Font.PLAIN / Font.BOLD
     * @param size 字号
     */
    private static Font getOsAutoFont(int style, int size) {

        String osName = System.getProperty("os.name").toLowerCase();
        String targetFontName;
        if (osName.contains("win")) {

            // Windows 使用微软雅黑
            targetFontName = "微软雅黑";

        } else {

            // Linux / Mac 使用文泉驿微米黑
            targetFontName = "WenQuanYi Micro Hei";
        }

        Font font = new Font(targetFontName, style, size);
        // 校验字体是否真实存在，不存在则兜底系统默认字体
        if (!targetFontName.equals(font.getFamily())) {
            return new Font("Dialog", style, size);
        }
        return font;
    }


}
