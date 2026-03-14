package jnpf.ureport.model;

import lombok.Data;

import java.util.*;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/8/23 上午8:53
 */
@Data
public class Formula {
    private String name;
    private int rowNumber;
    private int colNumber;
    private String formula;
    private List<String> cellNameList = new ArrayList<>();
    private Map<String,Map<Integer,Map<Integer,String>>> cellName = new HashMap<>();
}
