package jnpf.util.excel;

import jnpf.univer.model.UniverWorkBook;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/9/30 下午3:10
 */
public abstract class ExcelParser {
    public abstract UniverWorkBook formFile(InputStream inputStream) throws IOException;
}
