package jnpf.enums;

import lombok.Getter;

@Getter
public enum ColumnEnum {

    row("row", "列分栏"),
    col("col", "行分栏"),
    columnType("2", "1.数量分 2.分多少");

    private String type;
    private String name;

    ColumnEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }
}
