package au.com.mountainpass.ryvr.model;

import static de.otto.edison.hal.Link.*;

import java.util.HashMap;
import java.util.Map;

import de.otto.edison.hal.Link;

public class Entry extends MutableHalRepresentation {

    private Map<String, Object> properties = new HashMap<>();

    private Entry() {
    }

    public Entry(Link link, Map<String, Object> row) {
        properties = row;
        String id = row.get("ID").toString();
        super.add(linkBuilder("self", link.getHref() + "/" + id).withTitle(id)
                .build());
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

}
