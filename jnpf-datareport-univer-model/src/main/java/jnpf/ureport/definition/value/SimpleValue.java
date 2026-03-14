package jnpf.ureport.definition.value;


public class SimpleValue implements Value {
    private String value;

    public SimpleValue(String value) {
        this.value = value;
    }


    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getType() {
        return ValueType.simple.name();
    }
}
