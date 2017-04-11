package au.com.mountainpass;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.saucelabs.ci.sauceconnect.SauceConnectFourManager;
import com.saucelabs.ci.sauceconnect.SauceTunnelManager;

@Component
@Profile("sauceLabs")
public class SauceLabsTunnel implements DisposableBean {
    private Logger logger = LoggerFactory.getLogger(SauceLabsTunnel.class);

    @Value(value = "${webdriver.sauce.labs.username}")
    private String sauceUsername;

    @Value(value = "${webdriver.sauce.labs.key}")
    private String sauceAccessKey;

    @Value(value = "${webdriver.sauce.labs.verbose:false}")
    private boolean verboseLogging;

    @Value(value = "${webdriver.sauce.labs.port:4445}")
    private int port;

    @Value(value = "${webdriver.sauce.labs.options:}")
    private String options = "";

    private Process sauceConnectProcess = null;

    private SauceTunnelManager sauceConnectFourManager;

    @PostConstruct
    void connect() throws IOException {
        if (sauceConnectProcess == null) {
            logger.info("Starting Sauce Connect");
            sauceConnectFourManager = new SauceConnectFourManager(
                    !verboseLogging);
            try {
                sauceConnectProcess = sauceConnectFourManager.openConnection(
                        sauceUsername, sauceAccessKey, port, null, options,
                        null, verboseLogging, null);
            } catch (IOException e) {
                logger.error("Error generated when launching Sauce Connect", e);
                throw e;
            }
        }
    }

    @Override
    public void destroy() {
        if (sauceConnectFourManager != null) {
            logger.info("Stopping Sauce Connect");
            sauceConnectFourManager.closeTunnelsForPlan(this.sauceUsername,
                    options, null);
            logger.info("Sauce Connect stopped");
        }
    }
}
