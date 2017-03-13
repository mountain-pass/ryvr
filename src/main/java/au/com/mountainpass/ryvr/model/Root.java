package au.com.mountainpass.ryvr.model;

import static de.otto.edison.hal.Link.*;
import static de.otto.edison.hal.Links.*;

import de.otto.edison.hal.HalRepresentation;

public class Root extends HalRepresentation {

    public Root() {
        super(linkingTo(linkBuilder("self", "/").withTitle("Home").build(),
                linkBuilder("describedby", "/api-docs").withTitle("API Docs")
                        .build(),
                linkBuilder("https://ryvr.io/rels/ryvrs", "/ryvrs")
                        .withTitle("Ryvrs").build()));
    }
}
