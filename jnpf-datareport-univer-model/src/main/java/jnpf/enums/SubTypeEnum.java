package jnpf.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/11/27 上午11:46
 */
@Getter
public enum SubTypeEnum {

    cellIs("number", "cellIs"),
    equal("equal", "equal"),
    notEqual("notEqual", "notEqual"),
    top10("rank", "top10"),
    uniqueValues("uniqueValues", "uniqueValues"),
    duplicateValues("duplicateValues", "duplicateValues"),
    containsText("containsText", "containsText"),
    notContainsText("notContainsText", "notContainsText"),
    beginsWith("beginsWith", "beginsWith"),
    endsWith("endsWith", "endsWith"),
    containsBlanks("containsBlanks", "containsBlanks"),
    notContainsBlanks("notContainsBlanks", "notContainsBlanks"),
    containsErrors("containsErrors", "containsErrors"),
    notContainsErrors("notContainsErrors", "notContainsErrors"),
    expression("formula", "expression"),
    aboveAverage("average", "aboveAverage"),
    timePeriod("timePeriod", "timePeriod"),
    text("text", "text");


    private String code;
    private String type;

    SubTypeEnum(String code, String type) {
        this.code = code;
        this.type = type;
    }

    public static SubTypeEnum getType(String type) {
        for (SubTypeEnum status : SubTypeEnum.values()) {
            if (Objects.equals(status.getType(), type)) {
                return status;
            }
        }
        return null;
    }
}
