package jnpf.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/11/26 上午11:31
 */
@Data
public class DataQuery {
    private String sheet;
    private String sheetName;
    private List<Object> queryList = new ArrayList<>();
    private List<DataSortModel> sortList = new ArrayList<>();
}
