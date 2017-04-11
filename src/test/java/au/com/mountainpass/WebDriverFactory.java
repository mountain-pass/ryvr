package au.com.mountainpass;

import static org.openqa.selenium.remote.CapabilityType.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
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

import com.saucelabs.ci.sauceconnect.SauceConnectFourManager;
import com.saucelabs.ci.sauceconnect.SauceTunnelManager;

@Component
@Profile("uiTest")
public class WebDriverFactory implements DisposableBean {
    private Logger logger = LoggerFactory.getLogger(WebDriverFactory.class);

    @Value(value = "${webdriver.driver:org.openqa.selenium.chrome.ChromeDriver}")
    String driverClassName;

    @Value(value = "${webdriver.remote.url:null}")
    URL remoteUrl;

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

    private Process sauceConnectProcess = null;

    private SauceTunnelManager sauceConnectFourManager;

    public WebDriver createWebDriver()
            throws ClassNotFoundException, NoSuchMethodException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, IOException {
        DesiredCapabilities cap = new DesiredCapabilities();
        WebDriver driver = createDriver(cap);
        driver.manage().window().setSize(new Dimension(width, height));
        return driver;
    }

    private WebDriver createDriver(DesiredCapabilities cap)
            throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException, IOException {
        switch (driverClassName) {
        case "org.openqa.selenium.remote.RemoteWebDriver":
            setUpSauceConnect();
            cap.setCapability(BROWSER_NAME, BrowserType.CHROME);
            return new RemoteWebDriver(remoteUrl, cap);
        default:
            Class<?> driverClass = Class.forName(driverClassName);
            Constructor<?> constructor = driverClass
                    .getConstructor(Capabilities.class);
            return (WebDriver) constructor.newInstance(cap);
        }
    }

    private void setUpSauceConnect() throws IOException {
        if (sauceConnectProcess == null) {
            logger.info("Starting Sauce Connect");
            if (sauceUsername == null || sauceUsername.equals("")) {
                logger.error("Sauce sauceUsername not specified");
                return;
            }
            if (sauceAccessKey == null || sauceAccessKey.equals("")) {
                logger.error("Sauce access key not specified");
                return;
            }
            sauceConnectFourManager = new SauceConnectFourManager(false);
            try {
                sauceConnectProcess = sauceConnectFourManager.openConnection(
                        sauceUsername, sauceAccessKey, 4445, null, "", null,
                        true, null);
            } catch (IOException e) {
                logger.error("Error generated when launching Sauce Connect", e);
                throw e;
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
        shutdownSauceConnect();
    }

    private void shutdownSauceConnect() {

        if (sauceConnectProcess != null) {
            logger.info("Stopping Sauce Connect");
            sauceConnectFourManager.closeTunnelsForPlan(this.sauceUsername, "",
                    null);
            logger.info("Sauce Connect stopped");
        }
    }
}
