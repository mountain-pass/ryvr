package au.com.mountainpass.ryvr.model;

import static de.otto.edison.hal.Link.*;
import static de.otto.edison.hal.Links.*;

import de.otto.edison.hal.HalRepresentation;

public class Ryvr extends HalRepresentation {

    public Ryvr() {
        super(linkingTo(linkBuilder("self", "/").withTitle("Ryvr").build()));
    }

}
