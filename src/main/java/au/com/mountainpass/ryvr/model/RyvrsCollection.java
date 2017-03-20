package au.com.mountainpass.ryvr.model;

import static de.otto.edison.hal.Link.*;
import static de.otto.edison.hal.Links.*;

import org.springframework.stereotype.Component;

@Component
public class RyvrsCollection extends MutableHalRepresentation {

    private static final String TITLE = "Ryvrs";

    public RyvrsCollection() {
        super(linkingTo(
                linkBuilder("self", "/ryvrs").withTitle("Ryvrs").build()));
    }

    public int getCount() {
        return super.getEmbedded().getItemsBy("item").size();
    }

    public String getTitle() {
        return TITLE;
    }

}
