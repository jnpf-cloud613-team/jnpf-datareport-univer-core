package jnpf.univer.model;

import jnpf.univer.resources.UniverResource;
import jnpf.univer.sheet.UniverSheet;
import jnpf.univer.style.UniverStyle;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/5/11 下午4:35
 */
@Data
public class UniverWorkBook implements Serializable {
    private String id;
    private Integer rev;
    private String name;
    /**
     * Univer版本
     */
    private String appVersion = "0.4.1";
    /**
     * Univer语言
     */
    private String locale = "zhCN";
    /**
     * 样式列表
     */
    private Map<String, UniverStyle> styles = new HashMap<>();
    private List<String> sheetOrder = new ArrayList<>();
    private Map<String, UniverSheet> sheets = new HashMap<>();
    private List<UniverResource> resources = new ArrayList<>();
}
