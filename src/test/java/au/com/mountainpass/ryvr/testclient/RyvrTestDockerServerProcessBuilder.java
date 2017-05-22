package au.com.mountainpass.ryvr.testclient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = { "jarRun" })
public class RyvrTestDockerServerProcessBuilder
        implements RyvrTestServerProcessBuilder {

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
        return new ProcessBuilder("java", "-jar",
                "build/libs/ryvr-1.0.0-SNAPSHOT.jar",
                "--spring.config.location=" + configLocation);
    }

}
