package au.com.mountainpass;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Profile("uiTest")
public class WebDriverFactory implements DisposableBean {

    @Value(value = "${webdriver.driver:org.openqa.selenium.chrome.ChromeDriver}")
    String driverClassName;

    @Value(value = "${webdriver.window.width:1024}")
    int width;

    @Value(value = "${webdriver.window.height:768}")
    int height;

    @Autowired
    private AbstractApplicationContext context;

    public WebDriver createWebDriver()
            throws ClassNotFoundException, NoSuchMethodException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        DesiredCapabilities cap = new DesiredCapabilities();
        WebDriver driver = createDriver(cap);
        driver.manage().window().setSize(new Dimension(width, height));
        return driver;
    }

    private WebDriver createDriver(DesiredCapabilities cap)
            throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException {
        Class<?> driverClass = Class.forName(driverClassName);
        Constructor<?> constructor = driverClass
                .getConstructor(Capabilities.class);
        return (WebDriver) constructor.newInstance(cap);
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
