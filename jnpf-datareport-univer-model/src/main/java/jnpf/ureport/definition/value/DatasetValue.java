package jnpf.ureport.definition.value;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DatasetValue implements Value {

    private String datasetName;
    private AggregateType aggregate;
    private String property;
    private String groupType;
    private String sort;

    @Override
    public String getValue() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDatasetName());
        sb.append(".");
        sb.append(getAggregate().name());
        sb.append("(");
        String prop = getProperty();
        if (prop != null) {
            if (prop.length() > 13) {
                prop = prop.substring(0, 10) + "...";
            }
            sb.append(prop);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String getType() {
        return ValueType.dataset.name();
    }
}
