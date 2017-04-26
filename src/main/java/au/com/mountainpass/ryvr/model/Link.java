package au.com.mountainpass.ryvr.model;

import java.net.URI;

public class Link {

    private String href;
    private String title;

    public Link() {
        href = null;
        title = null;
    }

    public Link(String href, String title) {
        this.href = href;
        this.title = title;
    }

    public Link(String href) {
        this.href = href;
    }

    public static LinkBuilder fromUri(URI uri) {
        return new LinkBuilder(uri);
    }

    public String getTitle() {
        return this.title;
    }

    public String getHref() {
        return this.href;
    }

}
