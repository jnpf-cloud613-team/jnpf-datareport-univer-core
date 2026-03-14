package jnpf.enums;

import lombok.Getter;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.util.Objects;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2025/1/6 下午6:00
 */
@Getter
public enum HorizontalEnum {
    GENERAL(0, HorizontalAlignment.GENERAL),
    LEFT(1, HorizontalAlignment.LEFT),
    CENTER(2, HorizontalAlignment.CENTER),
    RIGHT(3, HorizontalAlignment.RIGHT);

    private Integer code;
    private HorizontalAlignment horizontal;

    HorizontalEnum(Integer code, HorizontalAlignment horizontal) {
        this.code = code;
        this.horizontal = horizontal;
    }

    public static HorizontalAlignment getHorizontalValue(Integer code) {
        for (HorizontalEnum status : HorizontalEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status.getHorizontal();
            }
        }
        return HorizontalAlignment.GENERAL;
    }

    public static Integer getHorizontalCode(HorizontalAlignment horizontal) {
        for (HorizontalEnum status : HorizontalEnum.values()) {
            if (Objects.equals(status.getHorizontal(), horizontal)) {
                return status.getCode();
            }
        }
        return null;
    }
}
