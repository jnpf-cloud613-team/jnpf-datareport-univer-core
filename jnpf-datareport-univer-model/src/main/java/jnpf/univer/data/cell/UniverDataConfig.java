package jnpf.univer.data.cell;

import jnpf.enums.CellDataEnum;
import jnpf.enums.UniverDataEnum;
import jnpf.enums.ZxingEnum;
import jnpf.univer.zxing.UniverZxingModel;
import jnpf.ureport.definition.value.AggregateType;
import lombok.Data;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/5/11 下午4:35
 */
@Data
public class UniverDataConfig {
    //dataSource
    private Integer col;
    private Integer row;
    private String sheet;
    private String type = CellDataEnum.text.name();
    private UniverDataConfig custom;
    private String field;
    //0.无 1.列表 2. 分组 3.汇总
    private Integer polymerizationType = 1;
    //汇总方式
    private String summaryType = AggregateType.sum.name();
    private String fillDirection = UniverDataEnum.cellDirection.getName();
    private String leftParentCellType = UniverDataEnum.cellNone.getName();
    private String leftParentCellCustomRowName;
    private String leftParentCellCustomColName;
    private String topParentCellType = UniverDataEnum.cellNone.getName();
    private String topParentCellCustomRowName;
    private String topParentCellCustomColName;
    //补充空白行
    private Boolean fillEmptyRows = false;
    private Integer fillEmptyNum = 1;
    //相邻连续分组
    private String groupType = UniverDataEnum.cellDefault.getName();
    //显示类型
    private String displayType = ZxingEnum.DEFAULT.getType();
    private UniverZxingModel qrCodeOption;
    private UniverZxingModel jsbarCodeOption;


    //echarts
    private String drawingId;
    private String unitId;
    private String subUnitId;
    private UniverDataConfig option;
    private String classifyNameField;
    private String seriesNameField;
    private String seriesDataField;
    private String maxField;

}
