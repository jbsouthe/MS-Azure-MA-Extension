import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.monitor.query.MetricsQueryClient;
import com.azure.monitor.query.MetricsQueryClientBuilder;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.QueryTimeInterval;

import java.time.Duration;
import java.util.List;

/**
 * See Docs: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-query
 */
public class AzMetricQuery {

    /**
     *
     * @param tokenCredential Azure TokenCredential
     * @param resourceId full resource identifier (ex: "/subscriptions/faa080af-c1d8-40ad-9cce-e1a450ca5b57/resourceGroups/srnagar-azuresdkgroup/providers/Microsoft.CognitiveServices/accounts/srnagara-textanalytics")
     * @param metricsNames List of metric names to query for
     * @param metricNamespace Namespace to query (ex: "Microsoft.Logic/Workflows")
     * @param timeInterval Interval of time to query metrics for (ex: Duration.ofDays(30))
     * @param granularity Metric aggregation granularity (ex: Duration.ofHours(1))
     * @param aggregations Metric Aggregations (i.e. AggregationType.AVERAGE, AggregationType.COUNT)
     *
     **/
    public static MetricsQueryResult getMetrics(TokenCredential tokenCredential,
                                   String resourceId,
                                   List<String> metricsNames,
                                   String metricNamespace,
                                   QueryTimeInterval timeInterval,
                                   Duration granularity,
                                   List<AggregationType> aggregations) {

        MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
                .credential(tokenCredential)
                .buildClient();

        Response<MetricsQueryResult> metricsResponse = metricsQueryClient
                .queryResourceWithResponse(
                        resourceId,
                        metricsNames,
                        new MetricsQueryOptions()
                                .setMetricNamespace(metricNamespace)
                                .setTimeInterval(timeInterval)
                                .setGranularity(granularity)
                                .setTop(100)
                                .setAggregations(aggregations),
                        Context.NONE);



        // Example for testing results (This assumes average and count aggregations used)
//        MetricsQueryResult metricsQueryResult = metricsResponse.getValue();
//        List<MetricResult> metrics = metricsQueryResult.getMetrics();
//        metrics.stream()
//                .forEach(metric -> {
//                    System.out.println(metric.getMetricName());
//                    System.out.println(metric.getId());
//                    System.out.println(metric.getResourceType());
//                    System.out.println(metric.getUnit());
//                    System.out.println(metric.getTimeSeries().size());
//                    System.out.println(metric.getTimeSeries().get(0).getValues().size());
//                    metric.getTimeSeries()
//                            .stream()
//                            .flatMap(ts -> ts.getValues().stream())
//                            .forEach(mv -> System.out.println(mv.getTimeStamp().toString()
//                                    + "; Count = " + mv.getCount()
//                                    + "; Average = " + mv.getAverage()));
//                });

        return metricsResponse.getValue();
    }
}