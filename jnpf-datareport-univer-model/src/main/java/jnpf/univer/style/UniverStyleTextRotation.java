package jnpf.univer.style;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/5/11 下午4:35
 */
@Data
public class UniverStyleTextRotation implements Serializable {
    /**
     * angle
     */
    private int a;
    /**
     * vertical
     * true : 1
     * false : 0
     */
    private int v;
}
