package au.com.mountainpass;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.apache.commons.lang.NotImplementedException;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Profile("uiTest")
public class WebDriverFactory implements DisposableBean {
    private Logger logger = LoggerFactory.getLogger(WebDriverFactory.class);

    @Value(value = "${webdriver.browser.type:chrome}")
    String browserName;

    @Value(value = "${webdriver.browser.type:}")
    String browserVersion;

    @Value(value = "${webdriver.window.width:1024}")
    int width;

    @Value(value = "${webdriver.window.height:768}")
    int height;

    @Autowired
    private AbstractApplicationContext context;

    @Value(value = "${webdriver.sauce.labs.username:}")
    private String sauceUsername;

    @Value(value = "${webdriver.sauce.labs.key:}")
    private String sauceAccessKey;

    @Value(value = "${BUILD_NUMBER:}")
    private String buildNumber;

    @Value(value = "${SHIPPABLE_REPO_SLUG:}")
    private String repoSlug;

    @Autowired(required = false)
    SauceLabsTunnel sauceLabsTunnel;

    public WebDriver createWebDriver()
            throws ClassNotFoundException, NoSuchMethodException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, IOException {
        DesiredCapabilities cap = new DesiredCapabilities();
        cap.setBrowserName(browserName);
        if (browserVersion != null && !browserVersion.isEmpty()) {
            cap.setCapability(CapabilityType.VERSION, browserVersion);
        }
        cap.setCapability("name", repoSlug + " - "
                + System.getProperty("spring.profiles.active"));
        cap.setCapability("tags", System.getProperty("spring.profiles.active"));
        // BUILD_URL
        if (buildNumber != null && !buildNumber.isEmpty()) {
            cap.setCapability("build", buildNumber);
        }

        WebDriver driver = createDriver(cap);
        driver.manage().window().setSize(new Dimension(width, height));
        return driver;
    }

    private WebDriver createDriver(DesiredCapabilities cap) throws IOException {
        if (sauceLabsTunnel != null) {
            URL remoteUrl = new URL("https://" + sauceUsername + ':'
                    + sauceAccessKey + "@ondemand.saucelabs.com:443/wd/hub");

            return new RemoteWebDriver(remoteUrl, cap);
        } else {
            switch (cap.getBrowserName()) {
            case BrowserType.CHROME:
                return new ChromeDriver(cap);
            default:
                throw new NotImplementedException(
                        "Browser Type: " + browserName);
            }
        }
    }

    @Override
    public void destroy() {
        // for some reason the destroy method on the webDriver bean was not
        // was not getting called, So we do it this way instead.
        context.getBeansOfType(WebDriver.class).forEach((key, value) -> {
            value.quit();
        });
    }

}
