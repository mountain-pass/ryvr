package au.com.mountainpass.ryvr.jdbc;

import static de.otto.edison.hal.Link.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import au.com.mountainpass.ryvr.model.Entry;
import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;

public class JdbcRyvr extends Ryvr {

    private static final int PAGE_SIZE = 10;
    private String table;
    private JdbcTemplate jt;
    private String orderedBy;

    public JdbcRyvr(String title, JdbcTemplate jt, String table,
            String orderedBy) {
        super(title);
        this.jt = jt;
        this.table = table;
        this.orderedBy = orderedBy;
    }

    @Override
    public void refresh() throws URISyntaxException {
        Integer count = jt.queryForObject(
                "select count(*) from \"" + table + "\"", Integer.class);
        jt.setFetchSize(PAGE_SIZE);
        SqlRowSet result = jt.queryForRowSet("select * from \"" + table
                + "\" ORDER BY \"" + orderedBy + "\" ASC");
        int page = (count - 1) / PAGE_SIZE;
        result.absolute(page * PAGE_SIZE + 1);

        List<HalRepresentation> embeddedItems = new ArrayList<>();
        List<Link> linkedItems = new ArrayList<>();

        String[] keyArray = result.getMetaData().getColumnNames();

        Optional<Link> selfLinkOptional = super.getLinks().getLinkBy("self");
        if (selfLinkOptional.isPresent()) {

            do {
                Map<String, Object> row = new HashMap<>();
                for (String key : keyArray) {
                    row.put(key, result.getObject(key));
                }
                Entry entry = new Entry(selfLinkOptional.get(), row, orderedBy);
                embeddedItems.add(entry);
                Link embeddedSelfLink = entry.getLinks().getLinkBy("self")
                        .get();
                linkedItems.add(linkBuilder("item", embeddedSelfLink.getHref())
                        .withTitle(embeddedSelfLink.getTitle()).build());
            } while (result.next());

            String selfHref = selfLinkOptional.get().getHref();

            linkedItems.add(linkBuilder("current", selfHref)
                    .withTitle("Current").build());
            addPageLink(linkedItems, selfHref, 1, "first", "First");
            addPageLink(linkedItems, selfHref, (count / PAGE_SIZE) + 1, "last",
                    "Last");
            if (page > 0) {
                addPageLink(linkedItems, selfHref, page - 1, "prev",
                        "Previous");
            }

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
