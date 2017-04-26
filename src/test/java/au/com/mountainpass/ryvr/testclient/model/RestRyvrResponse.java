package au.com.mountainpass.ryvr.testclient.model;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.traverson.Traverson;

public class RestRyvrResponse extends JavaRyvrResponse {

    private Traverson traverson;
    private URL contextUrl;
    private RestTemplate restTemplate;

    public RestRyvrResponse(Traverson traverson, URL contextUrl, Ryvr ryvr,
            RestTemplate restTemplate) {
        super(ryvr);
        this.traverson = traverson;
        this.contextUrl = contextUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    public RyvrResponse followLink(String rel) {
        try {
            URI ryvrUri = contextUrl.toURI()
                    .resolve(getRyvr().getLinks().get(rel).getHref());
            Ryvr ryvr = restTemplate.getForEntity(ryvrUri, Ryvr.class)
                    .getBody();
            return new RestRyvrResponse(traverson, ryvrUri.toURL(), ryvr,
                    restTemplate);
        } catch (MalformedURLException | URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new NotImplementedException();
        }
    }

}
