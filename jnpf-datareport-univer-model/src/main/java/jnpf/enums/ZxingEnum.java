package jnpf.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum ZxingEnum {
    BARCODE("jsbarcode","jsbarcode"),
    QRCODE("qrCode", "qrCode"),
    DEFAULT("default","default"),

    CODE128("code128","CODE_128"),
    EAN13("ean13","EAN_13"),
    EAN8("ean8","EAN_8"),
    CODE39("code39","CODE_39"),
    ITF("itf14","ITF"),
    CODABAR("codabar","CODABAR"),

    ;

    private String type;
    private String value;
    ZxingEnum(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public static ZxingEnum getType(String type) {
        for (ZxingEnum status : ZxingEnum.values()) {
            if (Objects.equals(status.getType(), type)) {
                return status;
            }
        }
        return ZxingEnum.DEFAULT;
    }
}
