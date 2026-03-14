package jnpf.univer.data.resource;

import lombok.Data;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/9/10 下午6:12
 */
@Data
public class UniverOffset {
    private Integer column;
    private Integer columnOffset;
    private Integer row;
    private Integer  rowOffset;
}
