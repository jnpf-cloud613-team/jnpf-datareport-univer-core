package jnpf.util;

import cn.hutool.core.img.ImgUtil;
import com.google.common.collect.ImmutableList;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import jnpf.enums.ZxingEnum;
import jnpf.univer.sheet.*;
import jnpf.univer.zxing.UniverZxingModel;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

@Slf4j
public class ZxingUtil {

    public static UniverZxingModel zxingImage(Map<Integer, UniverSheetRowData> sheetRowData, Map<Integer, UniverSheetColumnData> sheetColumnData, UniverSheet sheet, UniverSheetRange range, String displayType) {
        UniverZxingModel zxingModel = new UniverZxingModel();
        if (range != null) {
            List<Integer> heightList = new ArrayList<>();
            heightList.add(0);
            Integer startRow = range.getStartRow();
            Integer endRow = range.getEndRow();
            for (int i = startRow; i <= endRow; i++) {
                UniverSheetRowData rowData = sheetRowData.get(i);
                heightList.add(rowData != null ? rowData.getH() != null ? rowData.getH() : rowData.getAh() != null ? rowData.getAh() : sheet.getDefaultRowHeight() : sheet.getDefaultRowHeight());
            }
            List<Integer> widthList = new ArrayList<>();
            widthList.add(0);
            Integer startColumn = range.getStartColumn();
            Integer endColumn = range.getEndColumn();
            for (int i = startColumn; i <= endColumn; i++) {
                UniverSheetColumnData columnData = sheetColumnData.get(i);
                widthList.add(columnData != null && columnData.getW() != null ? columnData.getW() : sheet.getDefaultColumnWidth());
            }
            int heightSum = heightList.stream().mapToInt(Integer::intValue).sum();
            int widthSum = widthList.stream().mapToInt(Integer::intValue).sum();
            if (Objects.equals(ZxingEnum.QRCODE.getType(), displayType)) {
                int min = Collections.min(ImmutableList.of(widthSum, heightSum));
                zxingModel.setHeight(min * 5);
                zxingModel.setWidth(min * 5);
                zxingModel.setUniverHeight(min - 2);
                zxingModel.setUniverWidth(min - 2);
            } else {
                zxingModel.setHeight(heightSum / 2);
                zxingModel.setWidth(widthSum * 2);
                zxingModel.setWidth(200 - 10);
                zxingModel.setHeight(50);

                zxingModel.setUniverHeight(heightSum - 2);
                zxingModel.setUniverWidth(widthSum - 2);
            }
        }
        return zxingModel;
    }

    public static String zxing(UniverZxingModel zxingModel) {
        String base64 = "";
        ZxingEnum zxingEnum = ZxingEnum.getType(zxingModel.getDisplayType());
        BarcodeFormat format = BarcodeFormat.QR_CODE;
        switch (zxingEnum) {
            case QRCODE:
            case BARCODE:
                if (StringUtil.isNotEmpty(zxingModel.getFormat())) {
                    ZxingEnum type = ZxingEnum.getType(zxingModel.getFormat());
                    List<ZxingEnum> typeList = ImmutableList.of(ZxingEnum.ITF, ZxingEnum.EAN8, ZxingEnum.EAN13, ZxingEnum.BARCODE,
                            ZxingEnum.CODABAR, ZxingEnum.CODE128, ZxingEnum.CODE39);
                    if (typeList.contains(type)) {
                        format = BarcodeFormat.valueOf(type.getValue());
                    }
                }
                base64 = base64Image(format, zxingModel);
                break;
        }
        return base64;
    }

    private static String base64Image(BarcodeFormat format, UniverZxingModel zxingValue) {
        String baset64 = "";
        try {
            String value = zxingValue.getText();
            int height = zxingValue.getHeight();
            int width = zxingValue.getWidth();
            String level = zxingValue.getErrorCorrectionLevel();
            // 编码内容, 编码类型, 宽度, 高度, 设置参数
            Map<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 设置容错等级
            if (StringUtil.isNotEmpty(level)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.valueOf(level));
            }
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new MultiFormatWriter().encode(value, format, width, height, hints);

            // 动态计算增加的高度用于备注文本
            int textHeight = 5;
            String backgroundColor = zxingValue.getBackground();
            int background = rgb(backgroundColor, new Color(244, 245, 246).getRGB());
            String foregroundColor = zxingValue.getLineColor();
            int foreground = rgb(foregroundColor, new Color(0, 0, 0).getRGB());
            // 创建带有二维码和备注文本的
            BufferedImage image = new BufferedImage(width + textHeight, height + textHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(new Color(background));
            g.fillRect(0, 0, width + textHeight, height + textHeight);

            // 绘制二维码到 BufferedImage
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x + 3, y + 3, matrix.get(x, y) ? foreground : background);
                }
            }
            baset64 = "data:image/jpeg;base64," + ImgUtil.toBase64(image, "jpeg");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return baset64;
    }

    private static int rgb(String rgbCole, int imageColor) {
        Color color = null;
        try {
            color = new Color(Integer.parseInt(rgbCole.substring(1), 16));
        } catch (Exception e) {
        }
        return color == null ? imageColor : color.getRGB();
    }

    // 底部添加文本
    private static void addText(BufferedImage image, String text, int width, int height, int textHeight, int colorRgb) {
        Graphics2D g2 = image.createGraphics();
        g2.setColor(new Color(colorRgb));
        g2.setFont(new Font("Arial", Font.PLAIN, 20));  // 设置字体

        // 获取文本的宽度以便居中对齐
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = (width - textWidth) / 2;

        // 调整 y 坐标位置，将文本稍微上移
        int padding = 5;  // 增加一个 padding 值，让文本上移一点，避免贴得太近
        int y = height + (textHeight - fm.getHeight()) / 2 + fm.getAscent() - padding;

        // 绘制文本
        g2.drawString(text, x, y);
        g2.dispose(); // 释放资源
    }
}
