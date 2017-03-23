package au.com.mountainpass.ryvr.model;

import static de.otto.edison.hal.Link.*;
import static de.otto.edison.hal.Links.*;

public class Ryvr extends MutableHalRepresentation {

    private String title;

    private Ryvr() {
    }

    public Ryvr(String title) {
        super(linkingTo(linkBuilder("self", "/").withTitle("Ryvr").build()));
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

}
