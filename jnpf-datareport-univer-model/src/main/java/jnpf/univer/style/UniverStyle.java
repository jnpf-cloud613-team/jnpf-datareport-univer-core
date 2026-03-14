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
public class UniverStyle implements Serializable {
    /**
     * fontFamily
     */
    private String ff;
    /**
     * fontSize
     * <p>
     * pt
     */
    private Integer fs;
    /**
     * italic
     * 0: false
     * 1: true
     */
    private Integer it;
    /**
     * bold
     * 0: false
     * 1: true
     */
    private Integer bl;

    /**
     * underline
     */
    private UniverStyleTextDecoration ul;
    /**
     * strikethrough
     */
    private UniverStyleTextDecoration st;
    /**
     * overline
     */
    private UniverStyleTextDecoration ol;

    /**
     * background
     */
    private UniverStyleColor bg;

    /**
     * border
     */
    private UniverStyleBorder bd;

    /**
     * foreground
     */
    private UniverStyleColor cl;

    /**
     * (1.正常 2.Subscript 下标 3.Superscript上标 )
     */
    private Integer va;
    /**
     * textRotation
     */
    private UniverStyleTextRotation tr;
    /**
     * textDirection
     */
    private Integer td;
    /**
     * horizontalAlignment
     */
    private Integer ht;
    /**
     * verticalAlignment
     */
    private Integer vt;
    /**
     * wrapStrategy
     */
    private Integer tb;
    /**
     * padding
     */
    private UniverStylePadding pd;
    /**
     * dataFormat
     */
    private UniverStylePattern n;
}
