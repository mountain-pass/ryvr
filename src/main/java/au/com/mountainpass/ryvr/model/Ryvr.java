package au.com.mountainpass.ryvr.model;

import static de.otto.edison.hal.Link.*;
import static de.otto.edison.hal.Links.*;

import java.net.URISyntaxException;

import de.otto.edison.hal.Embedded;

public class Ryvr extends MutableHalRepresentation {

    private String title;

    private Ryvr() {
    }

    public Ryvr(String title) {
        super(linkingTo(linkBuilder("self", "/ryvrs/" + title).withTitle(title)
                .withName(title).build()));
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.otto.edison.hal.HalRepresentation#getEmbedded()
     */
    @Override
    public Embedded getEmbedded() {
        return super.getEmbedded();
    }

    public void refresh() throws URISyntaxException {

    }

}
