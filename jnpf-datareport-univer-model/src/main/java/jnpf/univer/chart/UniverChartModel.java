package jnpf.univer.chart;

import lombok.Data;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/9/10 下午1:26
 */
@Data
public class UniverChartModel {
    private String unitId;
    private String subUnitId;
    private String drawingId;
    private UniverChartField field = new UniverChartField();
    private String source;
}
