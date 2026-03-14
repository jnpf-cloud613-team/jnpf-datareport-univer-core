package jnpf.univer.zxing;

import lombok.Data;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/9/10 下午1:26
 */
@Data
public class UniverZxingModel {

    private String text;
    private Integer height;
    private String displayType;

    //条形码
    private String dark;
    private String light;
    private UniverZxingModel color;
    private String errorCorrectionLevel;

    //二维码
    private String format;
    private Boolean displayValue;
    private String lineColor;
    private String background;
    private Integer width;
    private Integer margin;
    private String font;
    private Integer fontSize;
    private String textAlign;
    private String textPosition;

    private Integer univerWidth;
    private Integer univerHeight;


}
