package cn.com.shopgroup.user.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QRCodeUtil {

    /**
     * 生成二维码 BufferedImage
     * @param content 二维码内容
     * @param width 宽
     * @param height 高
     */
    public static BufferedImage createQRCode(String content, int width, int height) throws WriterException {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        // 纠错级别 H最高
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1); // 白边大小

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    // 图片转 byte[]（接口返回流用）
    public static byte[] imageToBytes(BufferedImage image, String format) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, format, bos);
        return bos.toByteArray();
    }

    // 解析二维码内容
    public static String decodeQRCode(BufferedImage image) throws NotFoundException {

        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
    }

}