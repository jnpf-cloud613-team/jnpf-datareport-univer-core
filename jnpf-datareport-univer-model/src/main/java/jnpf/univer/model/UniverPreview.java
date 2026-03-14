package jnpf.univer.model;

import lombok.Data;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/7/4 下午4:12
 */
@Data
public class UniverPreview {

    private String cells;

    private String snapshot;

    private String queryList;

    private String chartData;

    private String fullName;

    private String versionId;

    private Integer allowExport;

    private Integer allowPrint;

    private Integer allowWatermark;

    private String watermarkConfig;

    private String columnList;

}
