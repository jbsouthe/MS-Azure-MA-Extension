import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

public class MSAzureMonitoring extends AManagedMonitor {
    private static final Logger logger = LogManager.getFormatterLogger(); //formatted logger allows for sprintf style ("%s %d",String, Number) calling conventions
    private String metricPrefix = "Custom Metrics|Azure|"; //the MA IGNORES custom metrics unless they start with 'Custom Metrics|' so make it happen programmatically
    private int metricsPrinted;
    private Configuration configuration;

    public MSAzureMonitoring() { //this is called only on initial load
        logger.info("Initializing Machine Agent MS Azure Extension");
    }

    @Override
    public TaskOutput execute(Map<String, String> configMap, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        try {
            this.configuration = new Configuration(configMap.get("config-file"));
        } catch (IOException e) {
            throw new TaskExecutionException("Error in configuration file parsing: "+e);
        }
        StringBuilder taskOutputStringBuilder = new StringBuilder();
        this.metricsPrinted=0;

        if( configuration.isLogicAppEnabled() ) {

            LogicAppMonitor logicAppMonitor = new LogicAppMonitor(configuration);
            for( Metric metric : logicAppMonitor.getMetrics() )
                printMetric(metric);

        }

        if( taskOutputStringBuilder.length() == 0 ) {
            taskOutputStringBuilder.append(String.format("Azure Monitor Ran With No Problems and printed %d Metrics", this.metricsPrinted));
            printMetric("Metrics Published", this.metricsPrinted);
        }
        return new TaskOutput(taskOutputStringBuilder.toString());
    }

    public void printMetric( String name, Object value ) { //some helpful defaults for ma jvm heap
        this.printMetric(name, value, MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION, MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT, MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);
    }

    public void printMetric( Metric metric ) {
        this.printMetric(metric.name, metric.value, metric.aggregation, metric.timeRollup, metric.cluster);
    }

    public long convertBytesToMegaBytes( long bytes ) { return bytes/1024/1024; }
    public void printMetric( String name, Object value, String aggregation, String timeRollup, String cluster ) { //the actual metric write
        MetricWriter metricWriter = getMetricWriter( this.metricPrefix + name, aggregation, timeRollup, cluster );
        metricWriter.printMetric(String.valueOf(value));
        metricsPrinted++;
    }
}
