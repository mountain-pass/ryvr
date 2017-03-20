package au.com.mountainpass.ryvr.model;

import java.util.Collections;
import java.util.List;

import de.otto.edison.hal.HalRepresentation;
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

    public void add(HalRepresentation embeddedItem) {
        super.withEmbedded("item", Collections.singletonList(embeddedItem));
    }
}
