package gui;

public class StatRow {

    private final String metric;
    private final String value;

    public StatRow(String metric, String value) {
        this.metric = metric;
        this.value = value;
    }

    public String getMetric() {
        return metric;
    }

    public String getValue() {
        return value;
    }
}
