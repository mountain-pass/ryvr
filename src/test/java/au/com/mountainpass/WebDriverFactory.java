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

    @Value(value = "${webdriver.driver:org.openqa.selenium.chrome.ChromeDriver}")
    String driverClassName;

    @Value(value = "${webdriver.remote.url:}")
    URL remoteUrl;

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

    @Value(value = "${webdriver.sauce.labs.username:null}")
    private String sauceUsername;

    @Value(value = "${webdriver.sauce.labs.key:null}")
    private String sauceAccessKey;

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
        WebDriver driver = createDriver(cap);
        driver.manage().window().setSize(new Dimension(width, height));
        return driver;
    }

    private WebDriver createDriver(DesiredCapabilities cap) throws IOException {
        if (sauceLabsTunnel != null) {
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
