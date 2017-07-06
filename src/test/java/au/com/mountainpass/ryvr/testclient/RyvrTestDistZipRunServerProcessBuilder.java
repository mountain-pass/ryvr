
package au.com.mountainpass.ryvr.testclient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = { "distZipRun" })
public class RyvrTestDistZipRunServerProcessBuilder implements RyvrTestServerProcessBuilder {

  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Value("${spring.datasource.password}")
  String springDatasourcePassword;

  @Value("${spring.datasource.url}")
  String springDatasourceUrl;

  @Value("${spring.datasource.username}")
  String springDatasourceUsername;

  protected static final String APPLICATION_YML = "build/distZipRun-application.yml";

  @Override
  public ProcessBuilder getProcessBuilder() {
    ProcessBuilder setupPb = new ProcessBuilder("bash", "./gradlew", "unzipDistZip").inheritIO();
    try {
      Process setupProcess = setupPb.start();
      setupProcess.waitFor(30, TimeUnit.SECONDS);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
    try (FileReader fr = new FileReader(APPLICATION_YML);
        FileWriter fw = new FileWriter("build/distributions/ryvr/etc/application.yml", true)) {
      int c = fr.read();
      while (c != -1) {
        fw.write(c);
        c = fr.read();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new ProcessBuilder("bash", "./gradlew", "distZipRun");
  }

  @Override
  public void createApplicationProperties(List<Map<String, String>> dataSourcesRyvrConfigs)
      throws IOException {
    new File(APPLICATION_YML).getParentFile().mkdirs();
    new File(APPLICATION_YML).delete();
    final FileWriter fileWriter = new FileWriter(APPLICATION_YML);
    final StringWriter writer = new StringWriter();
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
        writer.write("          catalog: " + config.get("database") + "\n");
        writer.write("          table: " + config.get("table") + "\n");
        writer.write("          ordered-by: " + config.get("ordered by") + "\n");
      }
    }
    writer.close();
    LOGGER.info("CONFIG:\r\n{}", writer.getBuffer().toString());
    fileWriter.write(writer.getBuffer().toString());
    fileWriter.close();
  }

}
