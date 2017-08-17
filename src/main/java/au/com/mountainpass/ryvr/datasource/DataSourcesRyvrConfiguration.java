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
import org.springframework.validation.annotation.Validated;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;

@Configuration
@ConfigurationProperties(prefix = "au.com.mountainpass.ryvr")
@Validated
public class DataSourcesRyvrConfiguration {

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private RyvrsCollection ryvrs;

  @Autowired
  private RyvrConfiguration rc;

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
   *          the items to set
   */
  public void setDataSources(List<DataSourceRyvrConfiguration> dataSourcesConfigs) {
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
      return dataSources.stream().map(config -> {
        DataSource dataSource = config.initializeDataSourceBuilder().build();
        return dataSource;
      }).collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  @Bean
  public Map<String, Ryvr> ryvrs() throws SQLException {
    Map<String, Ryvr> rval = new HashMap<>();
    if (dataSources != null) {
      for (int i = 0; i < dataSources.size(); ++i) {
        DataSourceRyvrConfiguration config = dataSources.get(i);
        if (config.getRyvrs() != null) {
          DataSource dataSource = dataSources().get(i);
          for (Entry<String, DataSourceRyvrConfigurationItem> dataSourceRyvrConfig : config
              .getRyvrs().entrySet()) {
            DataSourceRyvrConfigurationItem value = dataSourceRyvrConfig.getValue();
            DataSourceRyvrSource source = new DataSourceRyvrSource(dataSource, value.getQuery());
            rval.put(dataSourceRyvrConfig.getKey(),
                new Ryvr(dataSourceRyvrConfig.getKey(), value.getPageSize(), source));

          }
        }
      }
    }
    return rval;
  }

}
