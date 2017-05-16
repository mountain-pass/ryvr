package au.com.mountainpass.ryvr.datasource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;

import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;

@Configuration
@ConfigurationProperties(prefix = "au.com.mountainpass.ryvr")
@Validated
public class DataSourcesRyvrConfiguration {

    public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RyvrsCollection ryvrs;

    @Autowired
    @Qualifier("spring.datasource-org.springframework.boot.autoconfigure.jdbc.DataSourceProperties")
    private DataSourceProperties springDataSourceProperties;

    public DataSourcesRyvrConfiguration() {

    }

    @NestedConfigurationProperty
    @Valid
    private List<DataSourceRyvrConfiguration> dataSources;

    /**
     * @return the items
     */
    public List<DataSourceRyvrConfiguration> getDataSources() {
        return dataSources;
    }

    /**
     * @param items
     *            the items to set
     */
    public void setDataSources(
            List<DataSourceRyvrConfiguration> dataSourcesConfigs) {
        this.dataSources = dataSourcesConfigs;
    }

    @Bean
    @Primary
    public DataSourceProperties primaryDataSourceConfig() {
        if (dataSources != null) {
            return dataSources.get(0);
        }
        return springDataSourceProperties;
    }

    @Bean
    public List<DataSource> dataSources() {
        if (dataSources != null) {
            return dataSources.stream()
                    .map(config -> config.initializeDataSourceBuilder().build())
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Bean
    public List<JdbcTemplate> jdbcTemplates() {
        List<JdbcTemplate> jts = dataSources().stream()
                .map(ds -> new JdbcTemplate(ds)).collect(Collectors.toList());
        return jts;
    }

    @Bean
    public Map<String, Ryvr> ryvrs() throws SQLException {
        Map<String, Ryvr> rval = new HashMap<>();
        if (dataSources != null) {
            for (int i = 0; i < dataSources.size(); ++i) {
                DataSourceRyvrConfiguration config = dataSources.get(i);
                if (config.getRyvrs() != null) {
                    JdbcTemplate jdbcTemplate = jdbcTemplates().get(i);
                    for (Entry<String, DataSourceRyvrConfigurationItem> dataSourceRyvrConfig : config
                            .getRyvrs().entrySet()) {
                        DataSourceRyvr ryvr = new DataSourceRyvr(
                                dataSourceRyvrConfig.getKey(), jdbcTemplate,
                                dataSourceRyvrConfig.getValue().getCatalog(),
                                dataSourceRyvrConfig.getValue().getTable(),
                                dataSourceRyvrConfig.getValue().getOrderedBy(),
                                dataSourceRyvrConfig.getValue().getPageSize());
                        rval.put(dataSourceRyvrConfig.getKey(), ryvr);

                    }
                }
            }
        }
        return rval;
    }

}
