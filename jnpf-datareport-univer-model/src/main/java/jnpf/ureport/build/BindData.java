package jnpf.ureport.build;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;


@Setter
@Getter
public class BindData {
    private Object value;
    private List<Map<String, Object>> dataList;

}
