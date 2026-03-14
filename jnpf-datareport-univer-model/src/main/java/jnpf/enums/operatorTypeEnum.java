package jnpf.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/12/11 下午3:15
 */
@Getter
public enum operatorTypeEnum {

    none("none", -1),
    between("between", 0),
    notBetween("notBetween", 1),
    equal("equal", 2),
    NOT_EQUAL("notEqual", 3),
    greaterThan("greaterThan", 4),
    lessThan("lessThan", 5),
    greaterThanOrEqual("greaterThanOrEqual", 6),
    lessThanOrEqual("lessThanOrEqual", 7);

    private String operatorType;
    private Integer operator;

    operatorTypeEnum(String operatorType, Integer operator) {
        this.operatorType = operatorType;
        this.operator = operator;
    }

    public static operatorTypeEnum getOperator(String operatorType) {
        for (operatorTypeEnum status : operatorTypeEnum.values()) {
            if (Objects.equals(status.getOperatorType(), operatorType)) {
                return status;
            }
        }
        return operatorTypeEnum.none;
    }

    public static operatorTypeEnum getOperator(Integer operator) {
        for (operatorTypeEnum status : operatorTypeEnum.values()) {
            if (Objects.equals(status.getOperator(), operator)) {
                return status;
            }
        }
        return null;
    }
}
