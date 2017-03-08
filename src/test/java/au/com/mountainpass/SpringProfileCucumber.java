package au.com.mountainpass;

import java.io.IOException;

import org.junit.runners.model.InitializationError;
import org.springframework.test.context.ActiveProfiles;

import cucumber.api.junit.Cucumber;

public class SpringProfileCucumber extends Cucumber {
    public SpringProfileCucumber(Class clazz)
            throws InitializationError, IOException {
        super(clazz);
        ActiveProfiles ap = (ActiveProfiles) clazz
                .getAnnotation(ActiveProfiles.class);
        if (ap != null) {
            System.setProperty("spring.profiles.active",
                    String.join(",", ap.value()));
        }
    }
}
