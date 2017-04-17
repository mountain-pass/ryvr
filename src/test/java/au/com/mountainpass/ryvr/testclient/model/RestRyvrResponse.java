package au.com.mountainpass.ryvr.testclient.model;

import java.net.URL;

import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.traverson.Traverson;

public class RestRyvrResponse extends JavaRyvrResponse {

    public RestRyvrResponse(Traverson traverson, URL contextUrl, Ryvr ryvr) {
        super(ryvr);
    }

}
