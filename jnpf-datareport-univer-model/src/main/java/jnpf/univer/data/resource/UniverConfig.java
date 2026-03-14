package jnpf.univer.data.resource;

import lombok.Data;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/11/21 上午11:06
 */
@Data
public class UniverConfig {
    private String operator;
    private UniverValue value;
    private String iconType;
    private String iconId;
    private Integer index;
    private String color;

    private UniverValue min;
    private UniverValue max;
    private Boolean isGradient;
    private String positiveColor;
    private String nativeColor;


}
