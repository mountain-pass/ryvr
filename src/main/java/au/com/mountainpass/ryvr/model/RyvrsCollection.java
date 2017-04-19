package au.com.mountainpass.ryvr.model;

import static de.otto.edison.hal.Link.*;
import static de.otto.edison.hal.Links.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Component
public class RyvrsCollection extends MutableHalRepresentation {

    private static final String TITLE = "Ryvrs";

    private Map<String, Ryvr> ryvrs = new HashMap<>();

    public RyvrsCollection() {
        super(linkingTo(
                linkBuilder("self", "/ryvrs").withTitle("Ryvrs").build()));
    }

    public void addRyvr(Ryvr ryvr) {
        String name = ryvr.getTitle();
        ryvrs.put(name, ryvr);
        withLinks(linkBuilder("item",
                ryvr.getLinks().getLinkBy("self").get().getHref())
                        .withTitle(name).withName(name).build());
    }

    public int getCount() {
        return getLinks().getLinksBy("item").size();
    }

    public String getTitle() {
        return TITLE;
    }

    @JsonIgnore
    public Ryvr getRyvr(String ryvrName) {
        return ryvrs.get(ryvrName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.com.mountainpass.ryvr.model.MutableHalRepresentation#clear()
     */
    @Override
    public void clear() {
        ryvrs.clear();
        super.clear();
        withLinks(linkBuilder("self", "/ryvrs").withTitle("Ryvrs").build());

    }

}
