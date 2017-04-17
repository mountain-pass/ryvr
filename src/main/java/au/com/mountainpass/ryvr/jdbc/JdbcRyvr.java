package au.com.mountainpass.ryvr.jdbc;

import static de.otto.edison.hal.Link.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import au.com.mountainpass.ryvr.model.Entry;
import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;

public class JdbcRyvr extends Ryvr {

    private static final int PAGE_SIZE = 10;
    private String table;
    private JdbcTemplate jt;

    public JdbcRyvr(String title, JdbcTemplate jt, String table) {
        super(title);
        this.jt = jt;
        this.table = table;
    }

    @Override
    public void refresh() throws URISyntaxException {
        Integer count = jt.queryForObject(
                "select count(*) from \"" + table + "\"", Integer.class);
        List<Map<String, Object>> result = jt
                .queryForList("select * from \"" + table + "\"");
        jt.setMaxRows(PAGE_SIZE);
        List<HalRepresentation> embeddedItems = new ArrayList<>();
        List<Link> linkedItems = new ArrayList<>();

        result.parallelStream().forEach(row -> {
            Entry entry = new Entry(this.getLinks().getLinkBy("self").get(),
                    row);
            embeddedItems.add(entry);
            Link selfLink = entry.getLinks().getLinkBy("self").get();
            linkedItems.add(linkBuilder("item", selfLink.getHref())
                    .withTitle(selfLink.getTitle()).build());
        });

        Optional<Link> selfLinkOptional = super.getLinks().getLinkBy("self");
        if (selfLinkOptional.isPresent()) {
            String selfHref = selfLinkOptional.get().getHref();

            linkedItems.add(linkBuilder("current", selfHref)
                    .withTitle("Current").build());
            addPageLink(linkedItems, selfHref, 1, "first", "First");
            addPageLink(linkedItems, selfHref, (count / PAGE_SIZE) + 1, "last",
                    "Last");

        }
        withEmbedded("item", embeddedItems);
        withLinks(linkedItems);
    }

    public void addPageLink(List<Link> linkedItems, String baseUrl, int pageNo,
            String rel, String title) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(baseUrl);
        uriBuilder.addParameter("page", Integer.toString(pageNo));

        linkedItems.add(linkBuilder(rel, uriBuilder.build().toString())
                .withTitle(title).build());
    }

}
