package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.traverson.Traverson;

public class RestRyvrResponse extends JavaRyvrResponse {

    private Traverson traverson;
    private URL contextUrl;
    private RestTemplate restTemplate;
    private final HttpHeaders httpHeaders;

    public RestRyvrResponse(Traverson traverson, URL contextUrl, Ryvr ryvr,
            RestTemplate restTemplate, HttpHeaders httpHeaders) {
        super(ryvr);
        this.traverson = traverson;
        this.contextUrl = contextUrl;
        this.restTemplate = restTemplate;
        this.httpHeaders = httpHeaders;
    }

    @Override
    public RyvrResponse followLink(String rel) {
        try {
            URI ryvrUri = contextUrl.toURI()
                    .resolve(getRyvr().getLinks().get(rel).getHref());
            ResponseEntity<Ryvr> entityResponse = restTemplate
                    .getForEntity(ryvrUri, Ryvr.class);
            Ryvr ryvr = entityResponse.getBody();
            receivedBytes
                    .observe(entityResponse.getHeaders().getContentLength());
            return new RestRyvrResponse(traverson, ryvrUri.toURL(), ryvr,
                    restTemplate, entityResponse.getHeaders());
        } catch (MalformedURLException | URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new NotImplementedException();
        }
    }

    @Override
    public void assertFromCache() {
        assertThat(httpHeaders.get("X-Cache"), contains("HIT"));
    }

    @Override
    public void assertNotFromCache() {
        assertThat(httpHeaders.get("X-Cache"), not(contains("HIT")));
    }

}
