package jnpf.ureport.build;

import com.google.common.collect.ImmutableMap;
import jnpf.ureport.build.aggregate.*;
import jnpf.ureport.definition.value.AggregateType;
import jnpf.ureport.definition.value.DatasetValue;
import jnpf.ureport.model.Cell;

import java.util.List;
import java.util.Map;

public class DatasetUtils {

    private static final Map<AggregateType, Aggregate> aggregates = ImmutableMap.of(
            AggregateType.group, new GroupAggregate(),
            AggregateType.select, new SelectAggregate(),
            AggregateType.avg, new AvgAggregate(),
            AggregateType.count, new CountAggregate(),
            AggregateType.sum, new SumAggregate(),
            AggregateType.min, new MinAggregate(),
            AggregateType.max, new MaxAggregate(),
            AggregateType.none, new NoneAggregate()
    );


    public static List<BindData> computeDatasetExpression(DatasetValue datasetValue, Cell cell, Context context) {
        AggregateType type = datasetValue.getAggregate();
        Aggregate aggregate = aggregates.get(type);
        if (aggregate == null) {
            aggregate = aggregates.get(AggregateType.select);
        }
        return aggregate.aggregate(datasetValue, cell, context);
    }
}
