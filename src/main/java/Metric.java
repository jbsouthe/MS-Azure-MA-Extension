import com.singularity.ee.agent.systemagent.api.MetricWriter;

public class Metric {
    public String name = "Custom Metrics|Azure|", aggregation, timeRollup, cluster, value;
    public Metric(String name, String aggregation, String timeRollup, String cluster, String value) {
        this.name += name;
        this.aggregation = aggregation;
        this.timeRollup = timeRollup;
        this.cluster = cluster;
        this.value = value;
    }

    public Metric( String name, String value ){
        this.name += name;
        this.aggregation = MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION;
        this.timeRollup = MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT;
        this.cluster = MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL;
        this.value = value;
    }
}
