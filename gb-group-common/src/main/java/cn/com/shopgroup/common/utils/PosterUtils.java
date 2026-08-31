package cn.com.shopgroup.common.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

// 启动Jar增加参数-Djava.awt.headless=true用于Linux使用Graphics2D生成海报图片问题
public class PosterUtils {

    // 海报基础常量配置
    private static final int POSTER_WIDTH = 500;
    private static final int POSTER_HEIGHT = 400;
    private static final Color BG_COLOR = Color.decode("#04c455");
    private static final int PADDING = 10;
    private static final int GOODS_IMG_SIZE = 320;
    private static final int ROUND_RADIUS = 12;
    private static final int TOP_TEXT_AREA_HEIGHT = 40;
    private static final int PRICE_BTN_GAP = 20;


    // 生成海报，返回 ByteArrayInputStream
    public static ByteArrayInputStream generatePosterStream(PosterDTO dto) throws IOException {

        // 下载网络图片到内存字节数组
        byte[] goodsImgBytes = downloadNetImageToBytes(dto.getImgUrl());
        ByteArrayInputStream imgBis = new ByteArrayInputStream(goodsImgBytes);
        BufferedImage goodsImg = ImageIO.read(imgBis);
        imgBis.close();

        // 创建画布
        BufferedImage posterImg = new BufferedImage(POSTER_WIDTH, POSTER_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = posterImg.createGraphics();

        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 填充整体背景
        g2d.setColor(BG_COLOR);
        g2d.fillRect(0, 0, POSTER_WIDTH, POSTER_HEIGHT);

        // 绘制顶部文字区域（高度40px，文字垂直居中）
        drawTopTextArea(g2d, dto.getGoodsName(), dto.getSaleText());

        // 绘制底部白色圆角商品容器
        drawBottomGoodsArea(g2d, goodsImg, dto.getPriceText(), dto.getBtnText());

        // 销毁GD
        g2d.dispose();

        // 图片转字节流返回
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(posterImg, "png", bos);
        return new ByteArrayInputStream(bos.toByteArray());
    }

    // 顶部文字区域：固定高40px，文字垂直居中，左侧是商品名文本 右侧是销量文本
    private static void drawTopTextArea(Graphics2D g2d, String goodsName, String saleText) {

        // 设置字体和颜色
        Font font = getOsAutoFont(Font.PLAIN, 26);
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();

        // 顶部距离
        int areaStartY = PADDING;

        // 文字垂直居中基线计算
        int textAscent = fm.getAscent();
        int textHeight = fm.getHeight();
        int verticalOffset = (TOP_TEXT_AREA_HEIGHT - textHeight) / 2 + textAscent;
        int textY = areaStartY + verticalOffset;

        // 左侧商品名称
        g2d.drawString(goodsName, PADDING, textY);

        // 右侧销量右对齐
        int saleWidth = fm.stringWidth(saleText);
        g2d.drawString(saleText, POSTER_WIDTH - PADDING - saleWidth, textY);
    }

    /**
     * 底部白色圆角容器：320*320商品图
     * 右侧价格+按钮整体垂直居中，两者间距20px
     */
    private static void drawBottomGoodsArea(Graphics2D g2d, BufferedImage goodsImg, String priceText, String btnText) {

        // 底部容器坐标
        int containerX = PADDING;
        int containerY = PADDING + TOP_TEXT_AREA_HEIGHT;
        int containerW = POSTER_WIDTH - 2 * PADDING;
        int containerH = POSTER_HEIGHT - containerY - PADDING;

        // 绘制白色圆角底板
        RoundRectangle2D whiteRect = new RoundRectangle2D.Double(containerX, containerY, containerW, containerH, ROUND_RADIUS, ROUND_RADIUS);
        g2d.setColor(Color.WHITE);
        g2d.fill(whiteRect);

        // 缩放商品图至320*320
        Image scaleImg = goodsImg.getScaledInstance(GOODS_IMG_SIZE, GOODS_IMG_SIZE, Image.SCALE_SMOOTH);
        BufferedImage fixedImg = new BufferedImage(GOODS_IMG_SIZE, GOODS_IMG_SIZE, BufferedImage.TYPE_3BYTE_BGR);
        Graphics imgG = fixedImg.createGraphics();
        try {
            imgG.drawImage(scaleImg, 0, 0, null);
        } finally {
            imgG.dispose();
        }

        // 绘制商品图片（容器内padding10）
        int imgX = containerX + PADDING;
        int imgY = containerY + PADDING;
        g2d.drawImage(fixedImg, imgX, imgY, null);

        // 右侧文字起始X
        int rightX = imgX + GOODS_IMG_SIZE + 2;
        // 容器内有效绘图区域（扣除上下padding）
        int innerTop = containerY + PADDING;
        int innerBottom = containerY + containerH - PADDING;
        int innerHeight = innerBottom - innerTop;

        // 价格字体
        Font priceFont = getOsAutoFont(Font.BOLD, 28);
        FontMetrics priceFm = g2d.getFontMetrics(priceFont);

        // 计算两块文字总占用高度：价格文字高度 + 间距 + 按钮高度
        int priceTextH = priceFm.getHeight();
        int btnTotalH = 46; // 按钮固定高度
        int totalBlockHeight = priceTextH + PRICE_BTN_GAP + btnTotalH;

        // 整体垂直居中起始Y
        int blockStartY = innerTop + (innerHeight - totalBlockHeight) / 2;

        // 绘制价格文字
        g2d.setFont(priceFont);
        g2d.setColor(Color.RED);
        int priceBaseline = blockStartY + priceFm.getAscent();
        g2d.drawString(priceText, rightX, priceBaseline);

        // 按钮字体信息
        Font btnFont = getOsAutoFont(Font.PLAIN, 28);
        FontMetrics btnFm = g2d.getFontMetrics(btnFont);

        // 按钮Y坐标 = 价格区域底部 + 20px间距
        int btnY = blockStartY + priceTextH + PRICE_BTN_GAP;
        int btnTextW = btnFm.stringWidth(btnText);
        int btnW = btnTextW + 34;
        int btnH = btnTotalH;

        // 绘制绿色圆角按钮
        RoundRectangle2D btnRect = new RoundRectangle2D.Double(rightX, btnY, btnW, btnH, 18, 18);
        g2d.setColor(BG_COLOR);
        g2d.fill(btnRect);

        // 按钮白色文字居中
        g2d.setFont(btnFont);
        g2d.setColor(Color.WHITE);
        int textOffX = (btnW - btnTextW) / 2;
        int textOffY = btnH / 2 + btnFm.getAscent() / 2 - 4;
        g2d.drawString(btnText, rightX + textOffX, btnY + textOffY);
    }

    // 下载网络图片，返回图片字节数组（无本地文件）
    private static byte[] downloadNetImageToBytes(String urlStr) throws IOException {

        URL url = new URL(urlStr);
        try (InputStream is = url.openStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toByteArray();
        }
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
