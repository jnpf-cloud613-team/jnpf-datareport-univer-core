package jnpf.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/9/10 下午4:28
 */
@Getter
public enum CellDataTypeEnum {
    String(1), Number(2), Boolean(3), Formula(4);
    private Integer code;

    CellDataTypeEnum(int code) {
        this.code = code;
    }

    public static CellDataTypeEnum getDataType(Integer code) {
        for (CellDataTypeEnum status : CellDataTypeEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status;
            }
        }
        return CellDataTypeEnum.String;
    }

}
