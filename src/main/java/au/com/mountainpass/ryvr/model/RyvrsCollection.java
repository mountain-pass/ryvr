package au.com.mountainpass.ryvr.model;

import static de.otto.edison.hal.Link.*;
import static de.otto.edison.hal.Links.*;

import org.springframework.stereotype.Component;

import de.otto.edison.hal.HalRepresentation;

@Component
public class RyvrsCollection extends HalRepresentation {

    private static final String TITLE = "Ryvrs";

    public RyvrsCollection() {
        super(linkingTo(linkBuilder("self", "/").withTitle(TITLE).build()));
    }

    public int getCount() {
        return 0;
    }

    public String getTitle() {
        return TITLE;
    }

}
