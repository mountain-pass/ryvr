package au.com.mountainpass.ryvr.testclient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = { "bootRun" })
public class RyvrTestBootRunServerProcessBuilder implements RyvrTestServerProcessBuilder {

  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  static final String RUN_DIR = "build/bootrun";
  protected static final String APPLICATION_YML = RUN_DIR + "/application.yml";

  @Value("${spring.datasource.password}")
  String springDatasourcePassword;

  @Value("${spring.datasource.url}")
  String springDatasourceUrl;

  @Value("${spring.datasource.username}")
  String springDatasourceUsername;

  @Override
  public ProcessBuilder getProcessBuilder() {
    return new ProcessBuilder("bash", "./gradlew", "-Dspring.config.location=" + APPLICATION_YML,
        "bootRun");
  }

  @Override
  public void createApplicationProperties(List<Map<String, String>> dataSourcesRyvrConfigs)
      throws IOException {

    new File(RUN_DIR).mkdirs();
    new File(APPLICATION_YML).delete();
    final FileWriter fileWriter = new FileWriter(APPLICATION_YML);
    final StringWriter writer = new StringWriter();
    writer.write("server:\n");
    writer.write("  ssl:\n");
    writer.write("    key-store: build/bootrun/keystore.jks\n");
    writer.write("au.com.mountainpass.ryvr:\n");
    writer.write("  data-sources:\n");
    writer.write("    - url: " + springDatasourceUrl + "\n");
    writer.write("      username: " + springDatasourceUsername + "\n");
    writer.write("      password: " + springDatasourcePassword + "\n");
    if (!dataSourcesRyvrConfigs.isEmpty()) {
      writer.write("      ryvrs:\n");
      for (final Map<String, String> config : dataSourcesRyvrConfigs) {
        writer.write("        " + config.get("name") + ":\n");
        writer.write("          page-size: " + config.get("page size") + "\n");
        writer.write("          query: " + config.get("query") + "\n");
      }
    }
    writer.close();
    LOGGER.info("CONFIG:\r\n{}", writer.getBuffer().toString());
    fileWriter.write(writer.getBuffer().toString());
    fileWriter.close();
  }
}
