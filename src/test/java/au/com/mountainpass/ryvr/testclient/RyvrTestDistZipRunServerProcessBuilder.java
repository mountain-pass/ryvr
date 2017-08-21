
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
  private String springDatasourcePassword;

  @Value("${spring.datasource.url}")
  private String springDatasourceUrl;

  @Value("${spring.datasource.username}")
  private String springDatasourceUsername;

  protected static final String APPLICATION_YML = "build/distZipRun-application.yml";

  @Override
  public ProcessBuilder getProcessBuilder() throws IOException {
    ProcessBuilder setupPb = new ProcessBuilder("bash", "./gradlew", "unzipDistZip").inheritIO();
    try {
      Process setupProcess = setupPb.start();
      setupProcess.waitFor(30, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    try (FileReader fr = new FileReader(APPLICATION_YML);
        FileWriter fw = new FileWriter("build/distributions/ryvr/etc/application.yml", true)) {
      fw.write("\n");
      int c = fr.read();
      while (c != -1) {
        fw.write(c);
        c = fr.read();
      }
    }
    // make sure we don't let gradle unzip the dist again, as that would
    // overwrite our config changes
    String testClass = System.getProperty("au.com.mountainpass.testclass");
    LOGGER.info("au.com.mountainpass.testclass: {}", testClass);
    return new ProcessBuilder("bash", "./gradlew", "distZipRun", "-x", "unzipDistZip",
        "-Ptestclass=" + testClass);
  }

  @Override
  public void createApplicationProperties(List<Map<String, String>> dataSourcesRyvrConfigs)
      throws IOException {
    new File(APPLICATION_YML).getParentFile().mkdirs();
    new File(APPLICATION_YML).delete();
    final FileWriter fileWriter = new FileWriter(APPLICATION_YML);
    final StringWriter writer = new StringWriter();
    writer.write("au.com.mountainpass.ryvr.data-sources:\n");
    writer.write("  - url: " + springDatasourceUrl + "\n");
    writer.write("    username: " + springDatasourceUsername + "\n");
    writer.write("    password: " + springDatasourcePassword + "\n");
    if (!dataSourcesRyvrConfigs.isEmpty()) {
      writer.write("    ryvrs:\n");
      for (final Map<String, String> config : dataSourcesRyvrConfigs) {
        writer.write("      " + config.get("name") + ":\n");
        writer.write("        page-size: " + config.get("page size") + "\n");
        writer.write("        query: " + config.get("query") + "\n");
      }
    }
    writer.close();
    LOGGER.info("CONFIG:\r\n{}", writer.getBuffer().toString());
    fileWriter.write(writer.getBuffer().toString());
    fileWriter.close();
  }

}
