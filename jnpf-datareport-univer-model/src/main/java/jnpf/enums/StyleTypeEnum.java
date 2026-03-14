package jnpf.enums;

import lombok.Getter;
import org.apache.poi.ss.usermodel.BorderStyle;

import java.util.Objects;

@Getter
public enum StyleTypeEnum {

    NONE(0, BorderStyle.NONE),
    THIN(1, BorderStyle.THIN),
    HAIR(2, BorderStyle.HAIR),
    DOTTED(3, BorderStyle.DOTTED),
    DASHED(4, BorderStyle.DASHED),
    DASH_DOT(5, BorderStyle.DASH_DOT),
    DASH_DOT_DOT(6, BorderStyle.DASH_DOT_DOT),
    DOUBLE(7, BorderStyle.DOUBLE),
    MEDIUM(8, BorderStyle.MEDIUM),
    MEDIUM_DASHED(9, BorderStyle.MEDIUM_DASHED),
    MEDIUM_DASH_DOT(10, BorderStyle.MEDIUM_DASH_DOT),
    MEDIUM_DASH_DOT_DOT(11, BorderStyle.MEDIUM_DASH_DOT_DOT),
    SLANT_DASH_DOT(12, BorderStyle.SLANTED_DASH_DOT),
    THICK(13, BorderStyle.THICK);

    private Integer code;
    private BorderStyle borderStyle;

    StyleTypeEnum(Integer code, BorderStyle borderStyle) {
        this.code = code;
        this.borderStyle = borderStyle;
    }

    public static StyleTypeEnum getStyle(Integer code) {
        for (StyleTypeEnum status : StyleTypeEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status;
            }
        }
        return null;
    }

}
