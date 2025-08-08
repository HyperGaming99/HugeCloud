package eu.arolg.cloud.managers;

import java.io.IOException;
import java.util.Properties;

public class CloudManager {

    public Properties getproperties() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
