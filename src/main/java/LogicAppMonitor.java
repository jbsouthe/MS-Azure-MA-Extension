import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.monitor.query.MetricsQueryClient;
import com.azure.monitor.query.MetricsQueryClientBuilder;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.QueryTimeInterval;
import com.singularity.ee.agent.systemagent.api.MetricWriter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class LogicAppMonitor {
    private Configuration configuration;
    private MetricsQueryClient client;
    private String resourceId, namespace;
    private List<String> metricNames = new ArrayList<>();
    private QueryTimeInterval timeInterval;
    private Duration granularity;
    List<AggregationType> aggregations = new ArrayList<>();

    public LogicAppMonitor( Configuration configuration ) {
        this.configuration = configuration;
        this.client = new MetricsQueryClientBuilder()
                .credential(configuration.getCredential())
                .buildClient();
        this.resourceId = String.format("/subscriptions/%s/resourceGroups/%s/providers/Microsoft.CognitiveServices/accounts/srnagara-textanalytics");
        this.namespace = "Microsoft.Logic/Workflows";
        this.timeInterval = QueryTimeInterval.LAST_30_MINUTES;
        this.granularity = Duration.ofMinutes(1);
        this.aggregations.add(AggregationType.NONE);
    }

    public List<Metric> getMetrics() {

        Response<MetricsQueryResult> metricsResponse = client.queryResourceWithResponse(
                        resourceId,
                        this.metricNames,
                        new MetricsQueryOptions()
                            .setMetricNamespace(namespace)
                            .setTimeInterval(timeInterval)
                            .setGranularity(granularity)
                            .setTop(100)
                            .setAggregations(aggregations),
                            Context.NONE);

        // Create a list to store the metrics for all Logic Apps
        List<Metric> metrics = new ArrayList<>();

        MetricsQueryResult metricsQueryResult = metricsResponse.getValue();
        List<MetricResult> metricResults = metricsQueryResult.getMetrics();
        metricResults.stream()
               .forEach(metric -> {
                   System.out.println(metric.getMetricName());
                   System.out.println(metric.getId());
                   System.out.println(metric.getResourceType());
                   System.out.println(metric.getUnit());
                   System.out.println(metric.getTimeSeries().size());
                   System.out.println(metric.getTimeSeries().get(0).getValues().size());
                   metric.getTimeSeries()
                           .stream()
                           .flatMap(ts -> ts.getValues().stream())
                           .reduce((first, second) -> second)
                           .ifPresent(last -> metrics.add(
                               new Metric(
                                   String.format("Logic App|%s|%s", metric.getDescription(), metric.getMetricName()),
                                   MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                                   MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                                   MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL,
                                   String.valueOf(last.getTotal()))
                               )
                           );
               });

        return metrics;
    }

}
