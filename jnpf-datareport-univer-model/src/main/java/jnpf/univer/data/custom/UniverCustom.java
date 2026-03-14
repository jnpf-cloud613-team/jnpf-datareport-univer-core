package jnpf.univer.data.custom;

import jnpf.univer.data.cell.UniverDataConfig;
import lombok.Data;

import java.util.*;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/11/25 下午2:49
 */
@Data
public class UniverCustom {
    private List<UniverDataConfig> cells = new ArrayList<>();
    private Map<String, UniverDataConfig> floatEcharts = new HashMap<>();
    private Map<String, UniverDataConfig> cellEcharts = new HashMap<>();
    private Map<String, UniverDataConfig> floatImages = new HashMap<>();
}
