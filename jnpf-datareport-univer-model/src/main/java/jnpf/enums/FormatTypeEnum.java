package jnpf.enums;

import java.util.Objects;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/11/26 下午12:06
 */
public enum FormatTypeEnum {
    highlightCell, colorScale, iconSet, dataBar;

    public static FormatTypeEnum getFormat(String name) {
        for (FormatTypeEnum status : FormatTypeEnum.values()) {
            if (Objects.equals(status.name(), name)) {
                return status;
            }
        }
        return FormatTypeEnum.highlightCell;
    }
}
