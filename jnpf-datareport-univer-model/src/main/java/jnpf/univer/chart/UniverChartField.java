package jnpf.univer.chart;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/9/10 下午1:29
 */
@Data
public class UniverChartField {
    //维度
    private List<String> seriesNameField = new ArrayList<>();
    //系列
    private List<String> classifyNameField = new ArrayList<>();
    //值
    private List<List<String>> seriesDataField = new ArrayList<>();
    //最大值
    private List<String> maxField = new ArrayList<>();

}
