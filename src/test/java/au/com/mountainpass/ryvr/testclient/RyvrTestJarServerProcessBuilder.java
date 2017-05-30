package au.com.mountainpass.ryvr.testclient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = { "jarRun" })
public class RyvrTestJarServerProcessBuilder
        implements RyvrTestServerProcessBuilder {

    @Value("${au.com.mountainpass.ryvr.test.jar}")
    private String jarPath;

    @Override
    public ProcessBuilder getProcessBuilder(String configLocation) {
        ProcessBuilder setupPb = new ProcessBuilder("bash", "./gradlew",
                "bootRepackage").inheritIO();
        try {
            Process setupProcess = setupPb.start();
            setupProcess.waitFor(30, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new ProcessBuilder("java", "-jar", jarPath,
                "--spring.config.location=" + configLocation);
    }

}