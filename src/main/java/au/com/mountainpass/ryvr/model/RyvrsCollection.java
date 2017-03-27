package au.com.mountainpass.ryvr.model;

import static de.otto.edison.hal.Link.*;
import static de.otto.edison.hal.Links.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;

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
        final Links links = getLinks();
        List<Link> noItems = links.getRels().stream()
                .filter(rel -> !"item".equals(rel))
                .map(rel -> links.getLinkBy(rel).get())
                .collect(Collectors.toList());
        // TODO: super nasty. Refactor needed. Get it working first, then get it
        // right.
        try {
            Field original = HalRepresentation.class.getDeclaredField("links");
            original.setAccessible(true);
            original.set(this, Links.linkingTo(noItems));
        } catch (NoSuchFieldException | SecurityException
                | IllegalArgumentException | IllegalAccessException e) {
            throw new NotImplementedException(e);
        }
        super.clear();
    }

}
