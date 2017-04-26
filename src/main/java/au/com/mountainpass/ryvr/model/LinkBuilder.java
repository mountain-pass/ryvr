package au.com.mountainpass.ryvr.model;

import java.net.URI;

public class LinkBuilder {

    private String title;
    private URI href;

    public LinkBuilder(URI href) {
        this.href = href;
    }

    public LinkBuilder title(String title) {
        this.title = title;
        return this;
    }

    public Link build() {
        return new Link(href.toString(), title);
    }

}
