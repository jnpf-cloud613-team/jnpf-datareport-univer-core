package jnpf.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/11/21 下午3:15
 */
@Getter
public enum UniverDataEnum {

    //汇总方式
    none(0, "无"),
    select(1, "列表"),
    group(2, "分组"),
    summary(3, "汇总"),

    //填充方向
    cellDirection(4, "portrait"),
    cellDefault(5, "default"),
    cellNone(6, "none"),
    cellCustom(7, "custom"),
    cellRoot(8, "root"),

    //相邻连续分组
    adjacent(9, "adjacent"),

    ;

    private Integer code;
    private String name;

    UniverDataEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static UniverDataEnum getData(String name) {
        for (UniverDataEnum status : UniverDataEnum.values()) {
            if (Objects.equals(status.getName(), name)) {
                return status;
            }
        }
        return null;
    }
}
