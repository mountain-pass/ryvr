package au.com.mountainpass.ryvr.testclient.model;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.lang.NotImplementedException;

import au.com.mountainpass.ryvr.model.Entry;
import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.EmbeddedTypeInfo;
import de.otto.edison.hal.traverson.Traverson;

public class RestRyvrResponse extends JavaRyvrResponse {

    private Traverson traverson;
    private URL contextUrl;

    public RestRyvrResponse(Traverson traverson, URL contextUrl, Ryvr ryvr) {
        super(ryvr);
        this.traverson = traverson;
        this.contextUrl = contextUrl;
    }

    @Override
    public RyvrResponse followLink(String rel) throws URISyntaxException {
        Traverson followed = traverson.startWith(contextUrl, getRyvr())
                .follow(rel);
        EmbeddedTypeInfo embeddedTypeInfo = EmbeddedTypeInfo
                .withEmbedded("item", Entry.class);

        Optional<Ryvr> optionalRyvr = followed.getResourceAs(Ryvr.class,
                embeddedTypeInfo);
        if (optionalRyvr.isPresent()) {
            return new RestRyvrResponse(traverson,
                    followed.getCurrentContextUrl(), optionalRyvr.get());
        } else {
            throw new NotImplementedException(
                    followed.getLastError().getCause().get());
        }
    }

}
