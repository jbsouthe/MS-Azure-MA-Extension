import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class MSAzureMonitoring extends AManagedMonitor {
    private static final Logger logger = LogManager.getFormatterLogger(); //formatted logger allows for sprintf style ("%s %d",String, Number) calling conventions
    private String metricPrefix = "Custom Metrics|Azure|"; //the MA IGNORES custom metrics unless they start with 'Custom Metrics|' so make it happen programmatically
    private int metricsPrinted;

    public SelfMonitoring() { //this is called only on initial load
        logger.info("Initializing Machine Agent Heap Self Monitoring Extension");
    }

    @Override
    public TaskOutput execute(Map<String, String> configMap, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        StringBuilder taskOutputStringBuilder = new StringBuilder();
        this.metricsPrinted=0;
        printMetric( "Free Memory (MB)", convertBytesToMegaBytes(Runtime.getRuntime().freeMemory()));
        printMetric( "Current Usage (MB)", convertBytesToMegaBytes(Runtime.getRuntime().totalMemory()));
        printMetric( "Max Available (MB)", convertBytesToMegaBytes(Runtime.getRuntime().maxMemory()));
        long maxMemory = Runtime.getRuntime().maxMemory();
        if( maxMemory == 0 ) maxMemory++; //never get a divide by zero error
        long usedPercentage = (long) (Runtime.getRuntime().totalMemory() /(float)maxMemory  * 100); //convert the denominator to a float, we need the product to be a float
        if( usedPercentage < 0  || usedPercentage > 100 ) taskOutputStringBuilder.append(String.format("Error in used percentage calculation, this should never happen (0 >= %d <= 100)", usedPercentage));
        printMetric( "Used %", usedPercentage);
        if( taskOutputStringBuilder.length() == 0 ) taskOutputStringBuilder.append(String.format("Machine Agent Self Monitor Ran With No Problems and printed %d Metrics", this.metricsPrinted));
        return new TaskOutput(taskOutputStringBuilder.toString());
    }

    public void printMetric( String name, Object value ) { //some helpful defaults for ma jvm heap
        this.printMetric(name, value, MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION, MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT, MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);
    }

    public long convertBytesToMegaBytes( long bytes ) { return bytes/1024/1024; }
    public void printMetric( String name, Object value, String aggregation, String timeRollup, String cluster ) { //the actual metric write
        MetricWriter metricWriter = getMetricWriter( this.metricPrefix + name, aggregation, timeRollup, cluster );
        metricWriter.printMetric(String.valueOf(value));
        metricsPrinted++;
    }
}
