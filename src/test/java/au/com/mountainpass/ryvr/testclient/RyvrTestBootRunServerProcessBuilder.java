package au.com.mountainpass.ryvr.testclient;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = { "bootRun" })
public class RyvrTestBootRunServerProcessBuilder
        implements RyvrTestServerProcessBuilder {

    @Override
    public ProcessBuilder getProcessBuilder(String configLocation) {
        return new ProcessBuilder("bash", "./gradlew",
                "-Dspring.config.location=" + configLocation, "bootRun");
    }

}
