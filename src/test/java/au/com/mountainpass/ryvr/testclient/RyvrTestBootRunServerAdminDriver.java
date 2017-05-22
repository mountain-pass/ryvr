package au.com.mountainpass.ryvr.testclient;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = { "systemTest" })
public class RyvrTestBootRunServerAdminDriver
        extends RyvrTestExternalServerAdminDriver {

    @Override
    protected ProcessBuilder getProcessBuilder() {
        return new ProcessBuilder("bash", "./gradlew",
                "-Dspring.config.location=" + super.getApplicationYmlLocation(),
                "bootRun");
    }

}
