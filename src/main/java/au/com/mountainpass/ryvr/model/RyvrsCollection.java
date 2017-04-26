package au.com.mountainpass.ryvr.model;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class RyvrsCollection {

    private static final String TITLE = "Ryvrs";

    private Map<String, Ryvr> ryvrs = new HashMap<>();

    private Map<String, Link[]> links;

    public RyvrsCollection() {
        links = genLinks();
    }

    public void addRyvr(Ryvr ryvr) {
        String name = ryvr.getTitle();
        ryvrs.put(name, ryvr);
        links = genLinks();
    }

    public int getCount() {
        return links.get("item") == null ? 0 : links.get("item").length;
    }

    public String getTitle() {
        return TITLE;
    }

    public Ryvr getRyvr(String ryvrName) {
        return ryvrs.get(ryvrName);
    }

    public void clear() {
        ryvrs.clear();
        this.links = genLinks();
    }

    @JsonIgnore
    public Map<String, Ryvr> getRyvrs() {
        return ryvrs;
    }

    @JsonProperty("_links")
    public Map<String, Link[]> getLinks() {

        return links;

    }

    private Map<String, Link[]> genLinks() {
        Map<String, Link[]> rval = new HashMap<>();
        rval.put("self", new Link[] {
                Link.fromUri(URI.create("/ryvrs")).title(getTitle()).build() });
        if (!ryvrs.isEmpty()) {
            Link[] items = ryvrs.keySet().stream()
                    .map(key -> Link.fromUri(URI.create("/ryvrs/" + key))
                            .title(key).build())
                    .collect(Collectors.toList()).toArray(new Link[] {});
            rval.put("item", items);
        }
        return rval;
    }

}
