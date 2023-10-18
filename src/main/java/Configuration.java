import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

    private File configFile;
    private Properties properties;
    private TokenCredential credential = null;

    public Configuration( String configFileName ) throws IOException {
        this.configFile = new File(configFileName);
        if(!configFile.exists()) throw new IOException("Configuration File Not Found: "+ configFileName);
        if(!configFile.canRead()) throw new IOException("Configuration File Exists But Not Readable! "+ configFile.getAbsolutePath());
        this.properties = new Properties();
        FileInputStream inputStream = new FileInputStream("config.properties"));
        properties.load(inputStream);
    }

    private boolean isTrue( String property ) {
        return properties.getProperty(property, "false").equalsIgnoreCase("true");
    }

    public boolean isLogicAppEnabled() {
        return isTrue("enable-logicApps");
    }

    public TokenCredential getCredential() {
        if( this.credential == null ) {
            this.credential = new DefaultAzureCredentialBuilder()
                .build();
        }
        return this.credential;
    }
}
