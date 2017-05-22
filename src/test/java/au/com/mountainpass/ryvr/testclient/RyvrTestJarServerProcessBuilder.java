package au.com.mountainpass.ryvr.testclient;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import cucumber.api.PendingException;

@Component
@Profile(value = { "dockerRun" })
public class RyvrTestJarServerProcessBuilder
        implements RyvrTestServerProcessBuilder {

    @Override
    public ProcessBuilder getProcessBuilder(String configLocation) {
        throw new PendingException("TODO");
    }

}
