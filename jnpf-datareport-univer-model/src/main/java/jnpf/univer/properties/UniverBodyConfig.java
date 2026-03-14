
package jnpf.univer.properties;

import jnpf.univer.style.UniverStyle;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/11/5 下午4:08
 */
@Data
public class UniverBodyConfig {
    //customRanges
    private String rangeId;
    private Integer rangeType;
    private String url;
    private String refId;
    private Boolean wholeEntity;
    private UniverBodyConfig properties;

    //paragraphs
    private UniverBodyConfig paragraphStyle;

    //textRuns
    private Integer st;
    private Integer ed;
    private UniverStyle ts;

    //documentStyle
    private BigDecimal width;
    private BigDecimal height;
    private UniverBodyConfig pageSize;
    private Integer marginTop;
    private Integer marginBottom;
    private Integer marginLeft;
    private Integer marginRight;
    private UniverBodyConfig renderConfig;
    private Integer verticalAlign;
    private Integer centerAngle;
    private Integer vertexAngle;
    private Integer wrapStrategy;
    private Integer zeroWidthParagraphBreak;

    //settings
    private Integer zoomRatio;

    //customDecorations
    private String id;
    private UniverBodyConfig type;
    private UniverBodyConfig customDecorations;

    //paragraphs、documentStyle
    private Integer horizontalAlign;

    //sectionBreaks、customRanges、paragraphs、customDecorations、customBlocks
    private Integer startIndex;

    //customDecorations.customRanges
    private Integer endIndex;

    //customBlocks
    private String blockId;
}
