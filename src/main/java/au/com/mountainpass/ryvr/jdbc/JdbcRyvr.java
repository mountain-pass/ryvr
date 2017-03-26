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
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;

public class JdbcRyvr extends Ryvr {

    private String table;
    private JdbcTemplate jt;

    public JdbcRyvr(String title, JdbcTemplate jt, String table) {
        super(title);
        super.add(linkBuilder("current", "/").withTitle("Current").build());
        super.add(linkBuilder("last", "/").withTitle("Last").build());
        this.jt = jt;
        this.table = table;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.com.mountainpass.ryvr.model.Ryvr#getEmbedded()
     */
    @Override
    public Embedded getEmbedded() {
        CompletableFuture<HttpResponse> completableFuture = new CompletableFuture<HttpResponse>();
        List<Map<String, Object>> result = jt
                .queryForList("select * from \"" + table + "\"");
        List<HalRepresentation> embeddedItems = new ArrayList<>();

        result.parallelStream().forEach(row -> {
            embeddedItems.add(new Entry(row));
        });
        withEmbedded("item", embeddedItems);

        // TODO Auto-generated method stub
        return super.getEmbedded();
    }

}
