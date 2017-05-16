package au.com.mountainpass.ryvr.datasource;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

public class DataSourceRyvrConfiguration extends DataSourceProperties {

    @NestedConfigurationProperty
    @Valid
    private Map<String, DataSourceRyvrConfigurationItem> ryvrs;

    /**
     * @return the ryvrs
     */
    public Map<String, DataSourceRyvrConfigurationItem> getRyvrs() {
        return ryvrs;
    }

    /**
     * @param ryvrs
     *            the ryvrs to set
     */
    public void setRyvrs(Map<String, DataSourceRyvrConfigurationItem> ryvrs) {
        this.ryvrs = ryvrs;
    }

}