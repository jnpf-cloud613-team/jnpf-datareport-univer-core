package jnpf.consts;

import jnpf.config.ConfigValueUtil;
import jnpf.util.context.SpringContext;

public class ApiConst {

    private ApiConst(){}

    private static final ConfigValueUtil CONFIGVALUEUTIL = SpringContext.getBean(ConfigValueUtil.class);

    public static String ME = CONFIGVALUEUTIL.getApiDomain() + "/api/oauth/me";
    public static String DATASET_LIST = CONFIGVALUEUTIL.getApiDomain() + "/api/system/DataSet/getList";
    public static String DATASET_SAVE = CONFIGVALUEUTIL.getApiDomain() + "/api/system/DataSet/save";
    public static String DATASET_DATA = CONFIGVALUEUTIL.getApiDomain() + "/api/system/DataSet/Data";
    public static String SAVE_MENU = CONFIGVALUEUTIL.getApiDomain() + "/api/system/Menu/saveReportMenu";
    public static String GET_MENU = CONFIGVALUEUTIL.getApiDomain() + "/api/system/Menu/getReportMenu";
    public static String PARAMETER_DATA = CONFIGVALUEUTIL.getApiDomain() + "/api/system/DataSet/parameterData";
}
