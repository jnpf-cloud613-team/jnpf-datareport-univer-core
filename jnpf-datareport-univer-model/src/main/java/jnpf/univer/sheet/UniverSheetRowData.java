package jnpf.univer.sheet;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/5/11 下午4:35
 */
@Data
public class UniverSheetRowData implements Serializable {
    /**
     * height in pixel
     */
    private Integer h;
    /**
     * is current row self-adaptive to its content, use `ah` to set row height when true, else use `h`.
     */
    private Integer ia; // pre name `isAutoHeight`
    /**
     * auto height
     */
    private Integer ah;
    /**
     * hidden
     */
    private Integer hd;
}
