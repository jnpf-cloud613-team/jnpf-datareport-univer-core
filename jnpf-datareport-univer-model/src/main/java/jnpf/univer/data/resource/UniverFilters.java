package jnpf.univer.data.resource;

import lombok.Data;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/10/18 下午1:58
 */
@Data
public class UniverFilters {
    private UniverCustomFilters filters;
    private UniverCustomFilters customFilters;
    private Long colId;
}
