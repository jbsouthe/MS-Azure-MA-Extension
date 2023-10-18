import java.util.ArrayList;
import java.util.List;
import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.logicapps.LogicAppClient;
import com.azure.logicapps.implementation.LogicAppClientImpl;
import com.azure.logicapps.models.LogicApp;
import com.azure.logicapps.models.LogicAppMetrics;
import com.azure.logicapps.models.LogicAppMetricsListResult;
import com.singularity.ee.agent.systemagent.api.MetricWriter;

public class LogicAppMonitor implements Monitor{

    private LogicAppClient client;

    public LogicAppMonitor( TokenCredential credential ) {
        this.client = new LogicAppClientImpl(credential, AzureEnvironment.AZURE);
    }

    public List<Metric> getMetrics() {
        // Get the Azure credentials
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // Create a Logic App client
        LogicAppClient client = new LogicAppClientImpl(credential, AzureEnvironment.AZURE);

        // Get all Logic Apps in the subscription
        List<LogicApp> logicApps = client.listLogicApps();

        // Create a list to store the metrics for all Logic Apps
        List<Metric> metrics = new ArrayList<>();

        // Iterate over all Logic Apps and get their metrics
        for (LogicApp logicApp : logicApps) {
            // Get the metrics for the current Logic App
            LogicAppMetricsListResult logicAppMetrics = client.getLogicAppMetrics(logicApp.name(), Region.US_EAST2);

            for (LogicAppMetrics metric : logicAppMetrics.value()) {
                metrics.add( new Metric(
                        String.format("Logic App|%s|%s",metric.name(), metric.metricName()),
                        MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                        MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                        MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL,
                        metric.metricValue()));
        }

        return metrics;
    }
}
