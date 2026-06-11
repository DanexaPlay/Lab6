package main.Common;

import java.io.Serializable;

public class UpdateData implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private FlatData flatData;

    public UpdateData(long id, FlatData flatData) {
        this.id = id;
        this.flatData = flatData;
    }

    public long getId() {
        return id;
    }

    public FlatData getFlatData() {
        return flatData;
    }
}
