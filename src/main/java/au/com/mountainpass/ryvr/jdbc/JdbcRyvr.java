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
    private SqlRowSet rowSet;
    private String[] columnNames;
    private Long pages;

    public JdbcRyvr(String title, JdbcTemplate jt, String table,
            String orderedBy) {
        super(title);
        this.jt = jt;
        this.table = table;
        this.orderedBy = orderedBy;
    }

    @Override
    public void refresh() throws URISyntaxException {
        refresh(null);
    }

    @Override
    public void refresh(Long requestedPage) throws URISyntaxException {
        if (pages == null || requestedPage == null
                || requestedPage.equals(pages)) {
            final String countQuery = "select count(*) from \"" + table + "\"";
            Long count = new Long(
                    jt.queryForObject(countQuery, Integer.class).longValue());
            pages = ((count - 1) / PAGE_SIZE) + 1;
        }
        // hmmm... what happens when there are more rows than MAX_INT?
        Long page = (requestedPage == null ? pages : requestedPage);

        if (rowSet == null || page == pages) {
            jt.setFetchSize(PAGE_SIZE);
            String rowsQuery = "select * from \"" + table + "\" ORDER BY \""
                    + orderedBy + "\" ASC";
            rowSet = jt.queryForRowSet(rowsQuery);
            columnNames = rowSet.getMetaData().getColumnNames();
        }
        rowSet.absolute((int) ((page - 1) * PAGE_SIZE + 1));
        List<HalRepresentation> embeddedItems = new ArrayList<>();
        List<Link> linkedItems = new ArrayList<>();

        Optional<Link> selfLinkOptional = super.getLinks().getLinkBy("self");
        if (selfLinkOptional.isPresent()) {

            for (int i = 0; i < PAGE_SIZE; ++i) {
                Map<String, Object> row = new HashMap<>();
                for (int j = columnNames.length; j > 0; --j) {
                    row.put(columnNames[j - 1], rowSet.getObject(j));
                }
                Entry entry = new Entry(selfLinkOptional.get(), row, orderedBy);
                embeddedItems.add(entry);
                Link embeddedSelfLink = entry.getLinks().getLinkBy("self")
                        .get();
                linkedItems.add(linkBuilder("item", embeddedSelfLink.getHref())
                        .withTitle(embeddedSelfLink.getTitle()).build());
                if (!rowSet.next()) {
                    break;
                }
            }

            String selfHref = selfLinkOptional.get().getHref();

            addPageLink(linkedItems, selfHref, null, "current", "Current");
            addPageLink(linkedItems, selfHref, 1l, "first", "First");
            if (page > 1) {
                addPageLink(linkedItems, selfHref, page - 1, "prev",
                        "Previous");
            }
            if (page < pages) {
                addPageLink(linkedItems, selfHref, page + 1, "next", "Next");
            } else {
                // TODO: provide a lask like alias that redirects to the last
                // e.g. ...?page=last
                addPageLink(linkedItems, selfHref, pages, "last", "Last");
            }
            addPageLink(linkedItems, selfHref, requestedPage, "self",
                    selfLinkOptional.get().getTitle());
        }
        super.clear();

        withEmbedded("item", embeddedItems);
        withLinks(linkedItems);
    }

    public void addPageLink(List<Link> linkedItems, String baseUrl, Long pageNo,
            String rel, String title) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(baseUrl);
        uriBuilder.removeQuery();
        if (pageNo != null) {
            uriBuilder.addParameter("page", Long.toString(pageNo));
        }
        linkedItems.add(linkBuilder(rel, uriBuilder.build().toString())
                .withTitle(title).build());
    }

}
