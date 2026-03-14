package jnpf.ureport.definition.value;

public enum AggregateType {
    group, select, sum, avg, max, min, count, none;

    public static AggregateType value(String value) {
        for (AggregateType aggregate : AggregateType.values()) {
            if (aggregate.name().equalsIgnoreCase(value)) {
                return aggregate;
            }
        }
        return null;
    }
}
