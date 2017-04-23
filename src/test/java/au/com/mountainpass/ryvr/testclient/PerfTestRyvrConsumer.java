package au.com.mountainpass.ryvr.testclient;

import static de.otto.edison.hal.traverson.Traverson.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.traverson.Traverson;
import net.grinder.script.Grinder;
import net.grinder.script.InvalidContextException;

public class PerfTestRyvrConsumer {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PerfTestRyvrConsumer.class);

    private Traverson trav;
    private Ryvr ryvr;

    public PerfTestRyvrConsumer(Grinder.ScriptContext context)
            throws Exception {
        RestRyvrClient restRyvrClient = JythonShim.getRestRyvrClient();
        String startUrl = (String) context.getProperties()
                .get("grinder.startUrl");
        LOGGER.info("+++++++++ START URL: {} ++++++++++", startUrl);
        this.trav = traverson(restRyvrClient::httpGet).startWith(startUrl);
        this.ryvr = trav.getResourceAs(Ryvr.class).get();
        LOGGER.info("+++++++++ Current: {} ++++++++++", ryvr);

        this.ryvr = trav.startWith(trav.getCurrentContextUrl(), ryvr)
                .follow("first").getResourceAs(Ryvr.class).get();
        LOGGER.info("+++++++++ first: {} ++++++++++", ryvr);
    }

    public void call(Grinder.ScriptContext context)
            throws InvalidContextException {

        try {
            ryvr = trav.startWith(trav.getCurrentContextUrl(), ryvr)
                    .follow("next").getResourceAs(Ryvr.class).get();
            context.getStatistics().getForCurrentTest().setSuccess(true);
        } catch (Exception e) {
            context.getStatistics().getForCurrentTest().setSuccess(false);
        }
    }

    public void tearDown(Grinder.ScriptContext context) {

    }

}
