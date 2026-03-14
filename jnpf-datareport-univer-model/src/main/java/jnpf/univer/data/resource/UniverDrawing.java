package jnpf.univer.data.resource;

import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/5/11 下午4:35
 */
@Data
public class UniverDrawing implements Serializable {
    private String unitId;
    private String subUnitId;
    private String drawingId;
    private Integer drawingType;
    private String imageSourceType;
    private String source;
    private Boolean allowTransform;
    private String componentKey;
    //悬浮图片
    private UniverTransform sheetTransform;
    //单元格图片
    private UniverTransform docTransform;
    private UniverTransform transform;
    private Integer behindDoc;
    private String title;
    private String description;
    private Integer layoutType;
    private Integer wrapText;
    private Integer distB;
    private Integer distL;
    private Integer distR;
    private Integer distT;

    private UniverData data;
}
