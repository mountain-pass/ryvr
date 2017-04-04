package au.com.mountainpass.ryvr.jdbc;

import static de.otto.edison.hal.Link.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.http.HttpResponse;
import org.springframework.jdbc.core.JdbcTemplate;

import au.com.mountainpass.ryvr.model.Entry;
import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;

public class JdbcRyvr extends Ryvr {

    private String table;
    private JdbcTemplate jt;

    public JdbcRyvr(String title, JdbcTemplate jt, String table) {
        super(title);
        this.jt = jt;
        this.table = table;
    }

    @Override
    public void refresh() {
        CompletableFuture<HttpResponse> completableFuture = new CompletableFuture<HttpResponse>();
        List<Map<String, Object>> result = jt
                .queryForList("select * from \"" + table + "\"");
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
        withEmbedded("item", embeddedItems);
        withLinks(linkedItems);
    }

}
