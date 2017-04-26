package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.traverson.Traverson;

public class RestRyvrsCollectionResponse implements RyvrsCollectionResponse {

    private RyvrsCollection ryvrsCollection;
    private Traverson traverson;
    private URL contextUrl;
    private RestTemplate restTemplate;

    public RestRyvrsCollectionResponse(Traverson traverson, URL contextUrl,
            RyvrsCollection ryvrsCollection, RestTemplate restTemplate) {
        this.traverson = traverson;
        this.contextUrl = contextUrl;
        this.ryvrsCollection = ryvrsCollection;
        this.restTemplate = restTemplate;
    }

    @Override
    public void assertIsEmpty() {
        assertCount(0);
    }

    @Override
    public void assertCount(int count) {
        assertThat(ryvrsCollection.getCount(), equalTo(count));
    }

    @Override
    public void assertHasItem(List<String> names) {
        List<String> titles = Arrays
                .asList(ryvrsCollection.getLinks().get("item")).stream()
                .map(link -> link.getTitle()).collect(Collectors.toList());
        assertThat(titles, containsInAnyOrder(names.toArray()));
    }

    @Override
    public RyvrResponse followRyvrLink(String name) {
        try {
            URI ryvrUri = contextUrl.toURI().resolve("/ryvrs/" + name);
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

    private Predicate<Link> hasName(String name) {
        return itemLink -> {
            return name.equals(itemLink.getName());
        };
    }

}
