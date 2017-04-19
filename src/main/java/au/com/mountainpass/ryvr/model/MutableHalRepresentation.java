package au.com.mountainpass.ryvr.model;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;

public class MutableHalRepresentation extends HalRepresentation {

    public MutableHalRepresentation() {
        super();
    }

    public MutableHalRepresentation(Links linkingTo) {
        super(linkingTo);
    }

    public void add(List<? extends HalRepresentation> embeddedItems) {
        super.withEmbedded("item", embeddedItems);
    }

    public void add(HalRepresentation item) {
        super.withLinks(Link.copyOf(item.getLinks().getLinkBy("self").get())
                .withRel("item").build());
    }

    public void clear() {
        final Embedded embedded = getEmbedded();
        if (embedded != null && !embedded.isEmpty()) {
            // TODO: super nasty. Refactor needed. Get it working first, then
            // get it right.
            try {

                Field original = HalRepresentation.class
                        .getDeclaredField("embedded");
                original.setAccessible(true);
                original.set(this, Embedded.emptyEmbedded());
            } catch (NoSuchFieldException | SecurityException
                    | IllegalArgumentException | IllegalAccessException e) {
                throw new NotImplementedException(e);
            }
        }
        // TODO: super nasty. Refactor needed. Get it working first, then get it
        // right.
        try {
            Field original = HalRepresentation.class.getDeclaredField("links");
            original.setAccessible(true);
            original.set(this, Links.emptyLinks());
        } catch (NoSuchFieldException | SecurityException
                | IllegalArgumentException | IllegalAccessException e) {
            throw new NotImplementedException(e);
        }
    }

    public void add(Link link) {
        super.withLinks(link);
    }

}
