package jnpf.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/12/11 上午9:41
 */
@Getter
public enum ValidationType {

    none("any", 0),
    whole("whole", 1),
    decimal("decimal", 2),
    list("list", 3),
    checkbox("checkbox", 3),
    listMultiple("listMultiple", 3),
    date("date", 4),
    textLength("textLength", 6),
    custom("custom", 7);

    private String type;
    private Integer validationType;

    ValidationType(String type, Integer validationType) {
        this.type = type;
        this.validationType = validationType;
    }

    public static ValidationType getValidationType(String type) {
        for (ValidationType status : ValidationType.values()) {
            if (Objects.equals(status.getType(), type)) {
                return status;
            }
        }
        return ValidationType.none;
    }

    public static ValidationType getType(Integer validationType) {
        for (ValidationType status : ValidationType.values()) {
            if (Objects.equals(status.getValidationType(), validationType)) {
                return status;
            }
        }
        return ValidationType.none;
    }

}
