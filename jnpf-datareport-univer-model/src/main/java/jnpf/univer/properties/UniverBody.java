package jnpf.univer.properties;

import lombok.Data;

import java.util.List;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/11/5 下午3:58
 */
@Data
public class UniverBody {
    private String dataStream;
    private List<UniverBodyConfig> textRuns;
    private List<UniverBodyConfig> tables;
    private List<UniverBodyConfig> sectionBreaks;
    private List<UniverBodyConfig> paragraphs;
    private List<UniverBodyConfig> customDecorations;
    private List<UniverBodyConfig> customBlocks;
    private List<UniverBodyConfig> customRanges;
    private UniverBodyConfig settings;
}
