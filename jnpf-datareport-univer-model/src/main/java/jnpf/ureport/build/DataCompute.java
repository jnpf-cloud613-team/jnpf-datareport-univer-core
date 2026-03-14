package jnpf.ureport.build;

import com.google.common.collect.ImmutableMap;
import jnpf.ureport.compute.DatasetValueCompute;
import jnpf.ureport.compute.SimpleValueCompute;
import jnpf.ureport.compute.ValueCompute;
import jnpf.ureport.definition.value.Value;
import jnpf.ureport.definition.value.ValueType;
import jnpf.ureport.model.Cell;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DataCompute {

    private static Map<String, ValueCompute> valueComputesMap = ImmutableMap.of(
            ValueType.simple.name(), new SimpleValueCompute(),
            ValueType.dataset.name(), new DatasetValueCompute()
    );

    public static List<BindData> buildCellData(Cell cell, Context context) {
        Value value = cell.getValue();
        ValueCompute valueCompute = valueComputesMap.get(value.getType());
        if (valueCompute == null) {
            valueCompute = valueComputesMap.get(ValueType.simple);
        }
        return valueCompute.compute(cell, context);
    }
}
