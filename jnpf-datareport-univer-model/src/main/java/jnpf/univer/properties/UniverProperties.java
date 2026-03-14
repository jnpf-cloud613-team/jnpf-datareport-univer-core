package jnpf.univer.properties;

import jnpf.univer.data.resource.UniverDrawing;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/11/5 下午5:54
 */
@Data
public class UniverProperties {
    private UniverBody body;
    private Boolean disabled;
    private UniverDocumentStyle documentStyle;
    private Map<String, UniverDrawing> drawings;
    private List<String> drawingsOrder;
    private Map<String,Object> footers;
    private List<String> headerFooterDrawingsOrder;
    private Map<String,Object> headers;
    private String id;
    private Map<String,Object> lists;
    private String locale;
    private Map<String,Object> resources;
    private Integer rev;
    private UniverBodyConfig settings;
    private Map<String,Object> tableSource;
    private String title;
}
