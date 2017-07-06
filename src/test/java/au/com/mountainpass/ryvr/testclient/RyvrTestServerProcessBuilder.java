package au.com.mountainpass.ryvr.testclient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface RyvrTestServerProcessBuilder {

  public ProcessBuilder getProcessBuilder();

  public void createApplicationProperties(List<Map<String, String>> dataSourcesRyvrConfigs)
      throws IOException;

}
