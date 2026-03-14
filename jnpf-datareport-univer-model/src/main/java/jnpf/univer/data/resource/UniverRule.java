package jnpf.univer.data.resource;

import jnpf.univer.style.UniverStyle;
import lombok.Data;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/5/22 上午11:33
 */
@Data
public class UniverRule {
    private String type;
    private String subType;
    private String operator;
    private UniverStyle style;
    private Object value;
    private Boolean isShowValue;
    private Object config;
    private Boolean isPercent;
    private Boolean isBottom;
}
