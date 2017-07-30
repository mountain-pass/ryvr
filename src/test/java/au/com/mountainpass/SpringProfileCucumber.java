package au.com.mountainpass;

import java.io.IOException;

import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

import cucumber.api.junit.Cucumber;

public class SpringProfileCucumber extends Cucumber {
  private Logger logger = LoggerFactory.getLogger(SpringProfileCucumber.class);

  public SpringProfileCucumber(Class clazz) throws InitializationError, IOException {
    super(clazz);
    ActiveProfiles ap = (ActiveProfiles) clazz.getAnnotation(ActiveProfiles.class);
    if (ap != null) {
      String springProfilesActive = System.getProperty("spring.profiles.active");
      logger.info("initial spring.profiles.active: {}", springProfilesActive);
      String newActiveProfiles = String.join(",", ap.value());
      String activeProfiles = newActiveProfiles;
      if (springProfilesActive != null) {
        activeProfiles = String.join(",", springProfilesActive, newActiveProfiles);
      }
      System.setProperty("spring.profiles.active", activeProfiles);
      logger.info("current spring.profiles.active: {}", springProfilesActive);

      System.setProperty("au.com.mountainpass.testclass", clazz.getSimpleName());
    }
  }
}
