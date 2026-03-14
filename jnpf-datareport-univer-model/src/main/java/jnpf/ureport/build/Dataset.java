package jnpf.ureport.build;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;


@Setter
@Getter
public class Dataset {
    private String name;
    private List<Map<String, Object>> data;
}
