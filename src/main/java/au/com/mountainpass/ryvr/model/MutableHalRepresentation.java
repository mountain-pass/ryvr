package au.com.mountainpass.ryvr.model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.NotImplementedException;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Embedded.Builder;
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
            Builder eb = Embedded.embeddedBuilder();
            List<String> noItemsRels = embedded.getRels().stream()
                    .filter(rel -> !"item".equals(rel))
                    .collect(Collectors.toList());
            for (String rel : noItemsRels) {
                eb.with(rel, embedded.getItemsBy(rel));
            }
            // TODO: super nasty. Refactor needed. Get it working first, then
            // get it right.
            try {

                Field original = HalRepresentation.class
                        .getDeclaredField("embedded");
                original.setAccessible(true);
                original.set(this, eb.build());
            } catch (NoSuchFieldException | SecurityException
                    | IllegalArgumentException | IllegalAccessException e) {
                throw new NotImplementedException(e);
            }
        }
        final Links links = getLinks();
        List<Link> noItems = links.getRels().stream()
                .filter(rel -> !"item".equals(rel) && !"prev".equals(rel)
                        && !"next".equals(rel))
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
    }

    public void add(Link link) {
        super.withLinks(link);
    }

}
