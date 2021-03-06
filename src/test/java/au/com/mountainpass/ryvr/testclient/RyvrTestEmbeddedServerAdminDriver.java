package au.com.mountainpass.ryvr.testclient;

import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.datasource.DataSourceRyvrSource;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import au.com.mountainpass.ryvr.myob.MyobRyvrSource;
import cucumber.api.Scenario;

@Component
@Profile(value = { "integrationTest" })
public class RyvrTestEmbeddedServerAdminDriver implements RyvrTestServerAdminDriver {
  @Autowired
  private DataSource dataSource;

  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private RyvrsCollection ryvrsCollection;

  @Autowired
  private RyvrConfiguration rc;

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
    final DataSourceRyvrSource ryvr = new DataSourceRyvrSource(dataSource, config.get("query"));
    ryvrsCollection.put(config.get("name"),
        new Ryvr(config.get("name"), Integer.parseInt(config.get("page size")), ryvr));

  }

  @Override
  public void ensureStarted() {
    // nothing
  }

  @Override
  public void deleteRvyr(String name) {
    ryvrsCollection.remove(name);
  }

  @Override
  public void createMyobRyvr(Map<String, String> config) throws Throwable {
    MyobRyvrSource ryvr = new MyobRyvrSource();
    ryvrsCollection.put(config.get("name"),
        new Ryvr(config.get("name"), Integer.parseInt(config.get("page size")), ryvr));
  }

}
