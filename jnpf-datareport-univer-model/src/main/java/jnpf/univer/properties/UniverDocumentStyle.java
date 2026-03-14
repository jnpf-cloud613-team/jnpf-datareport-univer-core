package jnpf.univer.properties;

import jnpf.univer.style.UniverStyle;
import lombok.Data;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/11/5 下午3:55
 */
@Data
public class UniverDocumentStyle {

    private Integer autoHyphenation;
    private Integer characterSpacingControl;
    private Integer consecutiveHyphenLimit;
    private String defaultFooterId;
    private String defaultHeaderId;
    private Integer defaultTabStop;
    private Integer doNotHyphenateCaps;
    private Integer documentFlavor;
    private Integer evenAndOddHeaders;
    private String evenPageFooterId;
    private String evenPageHeaderId;
    private String firstPageFooterId;
    private String firstPageHeaderId;
    private Integer hyphenationZone;
    private Integer marginBottom;
    private Integer marginFooter;
    private Integer marginHeader;
    private Integer marginLeft;
    private Integer marginRight;
    private Integer marginTop;
    private Integer pageNumberStart;
    private Integer pageOrient;
    private UniverDocumentStyleConfig pageSize;
    private Integer paragraphLineGapDefault;
    private UniverDocumentStyleConfig renderConfig;
    private Boolean spaceWidthEastAsian;
    private UniverStyle textStyle;
    private Boolean useFirstPageHeaderFooter;
}
