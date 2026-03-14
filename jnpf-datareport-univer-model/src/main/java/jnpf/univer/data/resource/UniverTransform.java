package jnpf.univer.data.resource;

import lombok.Data;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/9/10 下午6:09
 */
@Data
public class UniverTransform {
    //悬浮图片
    private Boolean flipY;
    private Boolean flipX;
    private Integer skewX;
    private Integer skewY;
    private UniverOffset from;
    private UniverOffset to;

    //单元格图片
    private Integer relativeFrom;
    private Integer posOffset;
    private UniverTransform size;
    private UniverTransform positionH;
    private UniverTransform positionV;

    //图片公共
    private Integer angle;
    private Integer left;
    private Integer top;
    private Integer width;
    private Integer height;
}
