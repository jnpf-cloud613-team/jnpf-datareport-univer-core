package jnpf.ureport.definition;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class BlankCellInfo {
    private int offset;
    private int span;
    private boolean parent;

    public BlankCellInfo(int offset, int span, boolean parent) {
        this.offset = offset;
        this.span = span;
        this.parent = parent;
    }
}
