package au.com.mountainpass.ryvr.model;

import static de.otto.edison.hal.Link.*;
import static de.otto.edison.hal.Links.*;

import de.otto.edison.hal.HalRepresentation;

public class Root extends HalRepresentation {

    private String title;

    @SuppressWarnings("unused")
    private Root() {
    }

    public Root(String title) {
        super(linkingTo(linkBuilder("self", "/").withTitle("Home").build(),
                linkBuilder("describedby", "/api-docs").withTitle("API Docs")
                        .build(),
                linkBuilder("https://ryvr.io/rels/ryvrs-collection", "/ryvrs")
                        .withTitle("Ryvrs").build()));
        this.title = title;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setApplicationName(String applicationName) {
        this.title = applicationName;
    }

}
