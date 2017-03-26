package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.mountainpass.ryvr.model.Entry;
import au.com.mountainpass.ryvr.model.Ryvr;
import cucumber.api.PendingException;
import de.otto.edison.hal.Embedded;

public class JavaRyvrResponse implements RyvrResponse {
    private Ryvr ryvr;

    public JavaRyvrResponse(Ryvr ryvr) {
        this.ryvr = ryvr;
    }

    @Override
    public void assertHasEmbedded(List<Map<String, String>> events) {
        Embedded embedded = ryvr.getEmbedded();
        List<Entry> items = embedded.getItemsBy("item", Entry.class);
        for (int i = 0; i < items.size(); ++i) {
            final Map<String, String> expectedRow = events.get(i);
            items.get(i).getProperties().entrySet().forEach(entry -> {

                Object actualValue = entry.getValue();

                String expectedValue = expectedRow.get(entry.getKey());
                assertEqual(actualValue, expectedValue);
            });
        }
        assertThat(items.size(), equalTo(events.size()));
    }

    private void assertEqual(Object actualValue, String expectedValue) {
        if (actualValue instanceof BigDecimal) {
            assertThat((BigDecimal) actualValue,
                    closeTo(new BigDecimal(expectedValue), BigDecimal.ZERO));
        } else if (actualValue instanceof String) {
            assertThat(actualValue, equalTo(expectedValue));
        } else {
            Object expectedValueOfType;
            try {
                expectedValueOfType = actualValue.getClass()
                        .getConstructor(String.class)
                        .newInstance(expectedValue);
                assertThat(actualValue, equalTo(expectedValueOfType));
            } catch (InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                throw new PendingException();
            }
        }

    }

    @Override
    public void assertHasLinks(List<String> links) {
        Set<String> rels = ryvr.getLinks().getRels();
        links.forEach(item -> {
            assertThat(rels, hasItem(item));
        });
    }

    @Override
    public void assertDoesntHaveLinks(List<String> links) {
        Set<String> rels = ryvr.getLinks().getRels();
        links.forEach(item -> {
            assertThat(rels, not(hasItem(item)));
        });
    }
}
