package jnpf.enums;

import lombok.Getter;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.util.Objects;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2025/1/6 下午5:54
 */
@Getter
public enum VerticalEnum {
    TOP(1, VerticalAlignment.TOP),
    CENTER(2, VerticalAlignment.CENTER),
    BOTTOM(3, VerticalAlignment.BOTTOM);

    private Integer code;
    private VerticalAlignment vertical;

    VerticalEnum(Integer code, VerticalAlignment vertical) {
        this.code = code;
        this.vertical = vertical;
    }

    public static VerticalAlignment getVerticalValue(Integer code) {
        for (VerticalEnum status : VerticalEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status.getVertical();
            }
        }
        return null;
    }

    public static Integer getVerticalCode(VerticalAlignment vertical) {
        for (VerticalEnum status : VerticalEnum.values()) {
            if (Objects.equals(status.getVertical(), vertical)) {
                return status.getCode();
            }
        }
        return null;
    }

}
