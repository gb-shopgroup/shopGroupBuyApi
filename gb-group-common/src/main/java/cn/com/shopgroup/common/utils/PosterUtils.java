package cn.com.shopgroup.common.utils;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

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
    // 右下角店铺二维码尺寸/间距/提示语
    private static final int QR_CODE_SIZE = 110;
    private static final int QR_CODE_GAP = 8;
    private static final int QR_TIP_FONT_SIZE = 14;
    private static final String QR_TIP_TEXT = "长按识别进店";

    // ==================== 新版竖版商品海报(朋友圈分享样式)常量 ====================
    // 版式自上而下: 深绿店铺条 / 商品大图 / 团购活动名 / 商品名 / 价格+二维码 / 深绿保存提示条
    private static final int GOODS_POSTER_W = 500;
    private static final int GOODS_HEADER_H = 70;
    private static final int GOODS_IMG_H = 500;
    private static final int GOODS_GROUP_NAME_H = 60;
    private static final int GOODS_NAME_H = 60;
    private static final int GOODS_PRICE_H = 150;
    private static final int GOODS_FOOTER_H = 54;
    private static final int GOODS_POSTER_H = GOODS_HEADER_H + GOODS_IMG_H
            + GOODS_GROUP_NAME_H + GOODS_NAME_H + GOODS_PRICE_H + GOODS_FOOTER_H;
    private static final Color GOODS_HEADER_BG = Color.decode("#0A5B34");
    private static final Color GOODS_DARK_TEXT = Color.decode("#272727");
    private static final Color GOODS_GRAY_TEXT = Color.decode("#9b9b9b");
    private static final int GOODS_SIDE_PADDING = 20;
    private static final int GOODS_LOGO_SIZE = 46;
    private static final int GOODS_QR_SIZE = 130;
    private static final String DEFAULT_FOOTER_TEXT = "长按保存手机相册";


    /**
     * 生成新版竖版商品海报(朋友圈分享样式)，返回 ByteArrayInputStream
     * 版式自上而下: 深绿店铺条(logo+店铺名) / 商品大图(500*500居中裁剪)
     * / 团购活动名 / 商品名 / 价格(红色大字)+销量+右侧小程序码 / 深绿底部提示条(长按保存手机相册)
     */
    public static ByteArrayInputStream generateGoodsPosterStream(PosterDTO dto) throws IOException {

        // 商品大图: 居中裁剪为 500*500 正方形(不会拉伸)
        BufferedImage goodsImg = centerCropNetImage(dto.getImgUrl(), GOODS_POSTER_W, GOODS_IMG_H);

        // 创建画布
        BufferedImage posterImg = new BufferedImage(GOODS_POSTER_W, GOODS_POSTER_H, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = posterImg.createGraphics();

        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 白色底
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, GOODS_POSTER_W, GOODS_POSTER_H);

        // 1. 顶部深绿店铺条(logo + 店铺名)
        drawGoodsPosterHeader(g2d, dto, 0, GOODS_HEADER_H);

        // 2. 商品大图
        int goodsTop = GOODS_HEADER_H;
        g2d.drawImage(goodsImg, 0, goodsTop, null);

        // 3. 团购活动名(商品名上方, 为空时跳过该行)
        int groupNameTop = goodsTop + GOODS_IMG_H;
        if (dto.getGroupName() != null && dto.getGroupName().trim().length() > 0) {
            drawGoodsPosterGroupName(g2d, dto.getGroupName(), groupNameTop, GOODS_GROUP_NAME_H);
        }

        // 4. 商品名称
        int nameTop = groupNameTop + GOODS_GROUP_NAME_H;
        drawGoodsPosterName(g2d, dto.getGoodsName(), nameTop, GOODS_NAME_H);

        // 5. 价格 + 销量 + 右侧二维码
        int priceTop = nameTop + GOODS_NAME_H;
        drawGoodsPosterPriceArea(g2d, dto, priceTop, GOODS_PRICE_H);

        // 6. 底部深绿提示条
        int footerTop = GOODS_POSTER_H - GOODS_FOOTER_H;
        drawGoodsPosterFooter(g2d, dto.getFooterText(), footerTop, GOODS_FOOTER_H);

        // 销毁GD
        g2d.dispose();

        // 图片转字节流返回
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(posterImg, "png", bos);
        return new ByteArrayInputStream(bos.toByteArray());
    }

    // 顶部深绿店铺条: 左侧圆角logo(获取失败自动跳过), 右侧店铺名白色粗体, 垂直居中
    private static void drawGoodsPosterHeader(Graphics2D g2d, PosterDTO dto, int top, int height) {

        g2d.setColor(GOODS_HEADER_BG);
        g2d.fillRect(0, top, GOODS_POSTER_W, height);

        // 店铺logo: 居中裁剪为正方形 + 圆角, 下载失败不阻断海报生成
        int logoX = 16;
        int logoY = top + (height - GOODS_LOGO_SIZE) / 2;
        int nameX = logoX;
        try {
            String logoUrl = dto.getShopLogoUrl() == null ? "" : dto.getShopLogoUrl().trim();
            if (logoUrl.length() > 0) {
                BufferedImage logo = Thumbnails.of(new URL(logoUrl))
                        .size(GOODS_LOGO_SIZE, GOODS_LOGO_SIZE).crop(Positions.CENTER).asBufferedImage();
                g2d.drawImage(roundCornerImage(logo, 10), logoX, logoY, null);
                nameX = logoX + GOODS_LOGO_SIZE + 12;
            }
        } catch (Exception e) {
            // logo 获取失败仅跳过绘制
        }

        // 店铺名称(白色粗体, 垂直居中, 超宽省略)
        String shopName = ellipsize(dto.getShopName(), Font.BOLD, 26, GOODS_POSTER_W - nameX - 16);
        g2d.setFont(getOsAutoFont(Font.BOLD, 26));
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        int baseline = top + (height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(shopName, nameX, baseline);
    }

    // 团购活动名: 深色粗体左对齐, 垂直居中, 超宽省略(位于商品名上方)
    private static void drawGoodsPosterGroupName(Graphics2D g2d, String groupName, int top, int height) {

        String name = ellipsize(groupName, Font.BOLD, 28, GOODS_POSTER_W - 2 * GOODS_SIDE_PADDING);
        g2d.setFont(getOsAutoFont(Font.BOLD, 28));
        g2d.setColor(GOODS_DARK_TEXT);
        FontMetrics fm = g2d.getFontMetrics();
        int baseline = top + (height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(name, GOODS_SIDE_PADDING, baseline);
    }

    // 商品名称: 深色粗体左对齐, 垂直居中, 超宽省略
    private static void drawGoodsPosterName(Graphics2D g2d, String goodsName, int top, int height) {

        String name = ellipsize(goodsName, Font.BOLD, 30, GOODS_POSTER_W - 2 * GOODS_SIDE_PADDING);
        g2d.setFont(getOsAutoFont(Font.BOLD, 30));
        g2d.setColor(GOODS_DARK_TEXT);
        FontMetrics fm = g2d.getFontMetrics();
        int baseline = top + (height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(name, GOODS_SIDE_PADDING, baseline);
    }

    /**
     * 价格区域: 左侧 红色¥ + 价格大字 + 灰色单位, 下方灰色销量;
     * 右侧小程序码(白底打底保证识别率); 价格超宽时自动缩小数字字号
     */
    private static void drawGoodsPosterPriceArea(Graphics2D g2d, PosterDTO dto, int top, int height) {

        // 右侧小程序码(白底打底保证识别率)
        BufferedImage qrImg = dto.getQrCodeImage();
        int qrX = GOODS_POSTER_W - GOODS_SIDE_PADDING - GOODS_QR_SIZE;
        int qrY = top + (height - GOODS_QR_SIZE) / 2;
        int textRightLimit = GOODS_POSTER_W - GOODS_SIDE_PADDING;
        if (qrImg != null) {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(qrX - 4, qrY - 4, GOODS_QR_SIZE + 8, GOODS_QR_SIZE + 8);
            g2d.drawImage(scaleQrCodeImage(qrImg, GOODS_QR_SIZE), qrX, qrY, null);
            textRightLimit = qrX - 16;
        }

        // 价格行: ¥(30粗) + 价格数字(56粗) + 单位(22灰), 底部对齐
        String priceValue = dto.getPriceValue() == null || dto.getPriceValue().trim().length() == 0
                ? "0" : dto.getPriceValue().trim();
        String unit = dto.getPriceUnit() == null ? "" : dto.getPriceUnit().trim();
        int priceFontSize = 56;
        Font symbolFont = getOsAutoFont(Font.BOLD, 30);
        FontMetrics symbolFm = g2d.getFontMetrics(symbolFont);
        Font unitFont = getOsAutoFont(Font.PLAIN, 22);
        FontMetrics unitFm = g2d.getFontMetrics(unitFont);
        int maxTextW = textRightLimit - GOODS_SIDE_PADDING;
        // 价格数字过大时逐级缩小字号, 保证一行放下
        Font priceFont = getOsAutoFont(Font.BOLD, priceFontSize);
        FontMetrics priceFm = g2d.getFontMetrics(priceFont);
        int rowW = symbolFm.stringWidth("¥") + priceFm.stringWidth(priceValue)
                + (unit.length() > 0 ? unitFm.stringWidth(unit) + 4 : 0);
        while (rowW > maxTextW && priceFontSize > 30) {
            priceFontSize -= 4;
            priceFont = getOsAutoFont(Font.BOLD, priceFontSize);
            priceFm = g2d.getFontMetrics(priceFont);
            rowW = symbolFm.stringWidth("¥") + priceFm.stringWidth(priceValue)
                    + (unit.length() > 0 ? unitFm.stringWidth(unit) + 4 : 0);
        }

        int priceBaseline = top + 84;
        int x = GOODS_SIDE_PADDING;
        g2d.setFont(symbolFont);
        g2d.setColor(Color.RED);
        g2d.drawString("¥", x, priceBaseline);
        x += symbolFm.stringWidth("¥");

        g2d.setFont(priceFont);
        g2d.drawString(priceValue, x, priceBaseline);
        x += priceFm.stringWidth(priceValue);

        if (unit.length() > 0) {
            g2d.setFont(unitFont);
            g2d.setColor(GOODS_GRAY_TEXT);
            g2d.drawString(unit, x + 4, priceBaseline);
        }

        // 销量(灰色小字, 价格下方)
        String sales = dto.getSaleText() == null ? "" : dto.getSaleText().trim();
        if (sales.length() > 0) {
            g2d.setFont(getOsAutoFont(Font.PLAIN, 22));
            g2d.setColor(GOODS_GRAY_TEXT);
            String showSales = ellipsize(sales, Font.PLAIN, 22, maxTextW);
            g2d.drawString(showSales, GOODS_SIDE_PADDING, top + 126);
        }
    }

    // 底部深绿提示条: 白字水平垂直居中
    private static void drawGoodsPosterFooter(Graphics2D g2d, String footerText, int top, int height) {

        String text = footerText == null || footerText.trim().length() == 0
                ? DEFAULT_FOOTER_TEXT : footerText.trim();
        g2d.setColor(GOODS_HEADER_BG);
        g2d.fillRect(0, top, GOODS_POSTER_W, height);

        g2d.setFont(getOsAutoFont(Font.PLAIN, 24));
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        int textW = fm.stringWidth(text);
        int baseline = top + (height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, (GOODS_POSTER_W - textW) / 2, baseline);
    }

    // 网络图片居中裁剪为指定尺寸正方形区域(不会拉伸变形)
    private static BufferedImage centerCropNetImage(String imageUrl, int targetW, int targetH) throws IOException {

        return Thumbnails.of(new URL(imageUrl.trim()))
                .size(targetW, targetH).crop(Positions.CENTER).asBufferedImage();
    }

    // 正方形图片转圆角图片(保留透明通道, 用于店铺logo)
    private static BufferedImage roundCornerImage(BufferedImage source, int radius) {

        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setClip(new RoundRectangle2D.Double(0, 0, w, h, radius, radius));
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return out;
    }

    // 文本超宽省略: 超出最大宽度时从尾部截断并追加"…"
    private static String ellipsize(String text, int style, int size, int maxWidth) {

        String t = text == null ? "" : text.trim();
        if (t.length() == 0 || maxWidth <= 0) {
            return t;
        }
        Font font = getOsAutoFont(style, size);
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = tmp.createGraphics();
        FontMetrics fm = g.getFontMetrics(font);
        g.dispose();
        if (fm.stringWidth(t) <= maxWidth) {
            return t;
        }
        StringBuilder sb = new StringBuilder(t);
        while (sb.length() > 0 && fm.stringWidth(sb + "…") > maxWidth) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb + "…";
    }


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

        // 绘制底部白色圆角商品容器(含右下角团长店铺二维码)
        drawBottomGoodsArea(g2d, goodsImg, dto.getPriceText(), dto.getBtnText(), dto.getQrCodeImage());

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
     * qrCodeImage 非空时：右下角绘制团长店铺二维码+提示语，价格/按钮在二维码上方剩余区域垂直居中
     */
    private static void drawBottomGoodsArea(Graphics2D g2d, BufferedImage goodsImg, String priceText, String btnText, BufferedImage qrCodeImage) {

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

        // 价格字体
        Font priceFont = getOsAutoFont(Font.BOLD, 28);
        FontMetrics priceFm = g2d.getFontMetrics(priceFont);

        // 计算两块文字总占用高度：价格文字高度 + 间距 + 按钮高度
        int priceTextH = priceFm.getHeight();
        int btnTotalH = 46; // 按钮固定高度
        int totalBlockHeight = priceTextH + PRICE_BTN_GAP + btnTotalH;

        // 价格/按钮可用区域的底部(有店铺二维码时收缩到二维码提示语之上)
        int blockAreaBottom = innerBottom;

        // 右下角团长店铺二维码(缩放至110*110) + 上方提示语
        if (qrCodeImage != null) {
            int qrX = containerX + containerW - PADDING - QR_CODE_SIZE;
            int qrY = innerBottom - QR_CODE_SIZE;

            // 提示语: 二维码正上方居中
            Font tipFont = getOsAutoFont(Font.PLAIN, QR_TIP_FONT_SIZE);
            FontMetrics tipFm = g2d.getFontMetrics(tipFont);
            g2d.setFont(tipFont);
            g2d.setColor(Color.DARK_GRAY);
            int tipW = tipFm.stringWidth(QR_TIP_TEXT);
            int tipBaseline = qrY - QR_CODE_GAP;
            g2d.drawString(QR_TIP_TEXT, qrX + (QR_CODE_SIZE - tipW) / 2, tipBaseline);

            // 白底打底保证识别率, 再绘制二维码
            g2d.setColor(Color.WHITE);
            g2d.fillRect(qrX, qrY, QR_CODE_SIZE, QR_CODE_SIZE);
            g2d.drawImage(scaleQrCodeImage(qrCodeImage, QR_CODE_SIZE), qrX, qrY, null);

            blockAreaBottom = tipBaseline - tipFm.getAscent() - QR_CODE_GAP;
        }

        // 整体垂直居中起始Y
        int blockStartY = innerTop + (blockAreaBottom - innerTop - totalBlockHeight) / 2;

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

    // 缩放店铺二维码至固定尺寸; 保留透明通道(ARGB), 避免透明背景在海报上被填充成黑块
    private static BufferedImage scaleQrCodeImage(BufferedImage qrImg, int size) {

        Image scaleImg = qrImg.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        BufferedImage fixedImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics imgG = fixedImg.createGraphics();
        try {
            imgG.drawImage(scaleImg, 0, 0, null);
        } finally {
            imgG.dispose();
        }
        return fixedImg;
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
     *
     * @param style 字体样式 Font.PLAIN / Font.BOLD
     * @param size  字号
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
