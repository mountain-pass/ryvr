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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Profile("ui")
public class WebDriverFactory implements DisposableBean {

    @Value(value = "${webdriver.browserType:chrome}")
    private String browserType;

    @Value(value = "${webdriver.browserVersion:}")
    private String browserVersion;

    @Value(value = "${webdriver.window.width:1024}")
    private int width;

    @Value(value = "${webdriver.window.height:768}")
    private int height;

    @Autowired
    private AbstractApplicationContext context;

    @Value(value = "${webdriver.sauce.labs.username:}")
    private String sauceUsername;

    @Value(value = "${webdriver.sauce.labs.key:}")
    private String sauceAccessKey;

    @Value(value = "${BUILD_NUMBER:}")
    private String buildNumber;

    @Value(value = "${SHIPPABLE_REPO_SLUG:mountain-pass/ryvr}")
    private String repoSlug;

    @Value(value = "${webdriver.appiumVersion:}")
    private String appiumVersion;

    @Value(value = "${webdriver.deviceName:}")
    private String deviceName;

    @Value(value = "${webdriver.deviceOrientation:}")
    private String deviceOrientation;

    @Value(value = "${webdriver.platformVersion:}")
    private String platformVersion;

    @Value(value = "${webdriver.platformName:}")
    private String platformName;

    @Value(value = "${webdriver.browserName:}")
    private String browserName;

    @Autowired(required = false)
    private SauceLabsTunnel sauceLabsTunnel;

    public WebDriver createWebDriver()
            throws ClassNotFoundException, NoSuchMethodException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, IOException {
        DesiredCapabilities cap = (DesiredCapabilities) DesiredCapabilities.class
                .getMethod(browserType).invoke(null);

        setCapabilityIfSet(cap, CapabilityType.VERSION, browserVersion);

        cap.setCapability("name", repoSlug);
        cap.setCapability("tags", System.getProperty("spring.profiles.active"));
        // BUILD_URL
        setCapabilityIfSet(cap, "build", buildNumber);

        setCapabilityIfSet(cap, "appiumVersion", appiumVersion);
        setCapabilityIfSet(cap, "deviceName", deviceName);
        setCapabilityIfSet(cap, "deviceOrientation", deviceOrientation);
        setCapabilityIfSet(cap, "platformVersion", platformVersion);
        setCapabilityIfSet(cap, "platformName", platformName);
        setCapabilityIfSet(cap, "browserName", browserName);

        WebDriver driver = createDriver(cap);
        if (width != 0 && height != 0) {
            driver.manage().window().setSize(new Dimension(width, height));
        }
        return driver;
    }

    public void setCapabilityIfSet(DesiredCapabilities cap, String setting,
            String value) {
        if (value != null && !value.isEmpty()) {
            cap.setCapability(setting, value);
        }
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
                        "Browser Type: " + browserType);
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
