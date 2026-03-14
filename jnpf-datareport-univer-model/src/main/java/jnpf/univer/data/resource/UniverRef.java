package jnpf.univer.data.resource;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/5/22 上午11:41
 */
@Data
public class UniverRef implements Serializable {
    private Integer count;
    private String i;
    private String pattern;
    private String type;

}
