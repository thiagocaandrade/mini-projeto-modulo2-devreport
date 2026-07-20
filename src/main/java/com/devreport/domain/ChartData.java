package com.devreport.domain;

import java.util.Collections;
import java.util.List;

public class ChartData implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private final List<String> labels;
    private final List<Integer> values;

    public ChartData(List<String> labels, List<Integer> values) {
        this.labels = labels != null ? Collections.unmodifiableList(labels) : Collections.emptyList();
        this.values = values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<Integer> getValues() {
        return values;
    }
}
