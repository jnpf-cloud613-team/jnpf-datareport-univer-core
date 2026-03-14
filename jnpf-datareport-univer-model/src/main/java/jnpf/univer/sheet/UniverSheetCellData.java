package jnpf.univer.sheet;

import jnpf.ureport.model.Formula;
import lombok.Data;

import java.io.Serializable;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/5/11 下午4:35
 */
@Data
public class UniverSheetCellData implements Serializable {
    /**
     * The unique key, a random string, is used for the plug-in to associate the cell. When the cell information changes,
     * the plug-in does not need to change the data, reducing the pressure on the back-end interface id?: string.
     */
    private Object p;

    /**
     * style id
     * UniverStyle | String
     */
    private Object s;

    /**
     * Origin value
     * String | Integer | Boolean
     */
    private Object v;

    // Usually the type is automatically determined based on the data, or the user directly specifies
    // 1 string, 2 number, 3 boolean, 4 force string, green icon, set null for cell clear all
    private Integer t;

    /**
     * Raw formula string. For example `=SUM(A1:B4)`.
     */
    private String f;

}
