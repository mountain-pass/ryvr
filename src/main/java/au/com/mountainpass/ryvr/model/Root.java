package au.com.mountainpass.ryvr.model;

import static de.otto.edison.hal.Link.*;
import static de.otto.edison.hal.Links.*;

import de.otto.edison.hal.HalRepresentation;

public class Root extends HalRepresentation {

    public static final String RELS_RYVRS_COLLECTION = "https://mountain-pass.github.io/ryvr/rels/ryvrs-collection";
    private String title;

    @SuppressWarnings("unused")
    private Root() {
    }

    public Root(String title) {
        super(linkingTo(linkBuilder("self", "/").withTitle("Home").build(),
                linkBuilder("describedby", "/api-docs").withTitle("API Docs")
                        .build(),
                linkBuilder(
                        RELS_RYVRS_COLLECTION,
                        "/ryvrs").withTitle("Ryvrs").build()));
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
