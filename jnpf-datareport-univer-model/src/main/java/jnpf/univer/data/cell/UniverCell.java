package jnpf.univer.data.cell;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/8/1 下午8:54
 */
@Data
public class UniverCell implements Serializable {
    private String sheet;
    private Integer row;
    private Integer col;
    private String drawingId;
    private String domId;
}
