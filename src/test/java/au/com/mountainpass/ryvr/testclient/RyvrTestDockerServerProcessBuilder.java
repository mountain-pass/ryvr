package au.com.mountainpass.ryvr.testclient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = { "dockerRun" })
public class RyvrTestDockerServerProcessBuilder implements RyvrTestServerProcessBuilder {

  @Override
  public ProcessBuilder getProcessBuilder() {
    throw new NotImplementedException("TODO");
  }

  @Override
  public void createApplicationProperties(List<Map<String, String>> dataSourcesRyvrConfigs)
      throws IOException {
    throw new NotImplementedException("TODO");
  }

}
