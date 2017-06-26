package au.com.mountainpass.ryvr.testclient;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import au.com.mountainpass.ryvr.datasource.DataSourceRyvrSource;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import cucumber.api.Scenario;

@Component
@Profile(value = { "integrationTest" })
public class RyvrTestEmbeddedServerAdminDriver implements RyvrTestServerAdminDriver {
  @Autowired
  private JdbcTemplate currentJt;

  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private RyvrsCollection ryvrsCollection;

  @Override
  public void _after(final Scenario scenario) {
    // nothing
  }

  @Override
  public void _before(final Scenario scenario) {
    clearRyvrs();
  }

  @Override
  public void clearRyvrs() {
    ryvrsCollection.clear();
  }

  @Override
  public void createDataSourceRyvr(final Map<String, String> config) throws Throwable {
    final DataSourceRyvrSource ryvr = new DataSourceRyvrSource(currentJt, config.get("database"),
        config.get("table"), config.get("ordered by"), Integer.parseInt(config.get("page size")));
    ryvrsCollection
        .addRyvr(new Ryvr(config.get("name"), Integer.parseInt(config.get("page size")), ryvr));

  }

  @Override
  public void ensureStarted() {
    // nothing
  }

}
