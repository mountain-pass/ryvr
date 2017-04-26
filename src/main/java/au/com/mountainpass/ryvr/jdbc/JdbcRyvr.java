package au.com.mountainpass.ryvr.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import au.com.mountainpass.ryvr.model.Link;
import au.com.mountainpass.ryvr.model.Ryvr;

public class JdbcRyvr extends Ryvr {

    private static final int PAGE_SIZE = 10;
    private String table;
    private JdbcTemplate jt;
    private String orderedBy;
    private SqlRowSet rowSet;
    private String[] columnNames;

    public JdbcRyvr(String title, JdbcTemplate jt, String table,
            String orderedBy) {
        super(title);
        this.jt = jt;
        this.table = table;
        this.orderedBy = orderedBy;
        refresh();
    }

    @Override
    public void refresh() {
        refreshPage(null);
    }

    @Override
    public void refreshPage(Long requestedPage) {
        if (requestedPage != null && requestedPage < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (pages == null || requestedPage == null || requestedPage >= pages) {
            final String countQuery = "select count(*) from \"" + table + "\"";
            Long count = new Long(
                    jt.queryForObject(countQuery, Integer.class).longValue());
            pages = ((count - 1) / PAGE_SIZE) + 1;
        }
        if (requestedPage != null && requestedPage > pages) {
            throw new IndexOutOfBoundsException();
        }

        // hmmm... what happens when there are more rows than MAX_INT?
        page = requestedPage == null ? pages : requestedPage;

        if (rowSet == null || page == pages) {
            jt.setFetchSize(PAGE_SIZE);
            String rowsQuery = "select * from \"" + table + "\" ORDER BY \""
                    + orderedBy + "\" ASC";
            rowSet = jt.queryForRowSet(rowsQuery);
            columnNames = rowSet.getMetaData().getColumnNames();
        }
        rowSet.absolute((int) ((page - 1) * PAGE_SIZE + 1));

        rows.clear();
        for (int i = 0; i < PAGE_SIZE; ++i) {
            Map<String, Object> row = new HashMap<>();
            for (int j = columnNames.length; j > 0; --j) {
                row.put(columnNames[j - 1], rowSet.getObject(j));
            }
            List<Map<String, Object>> itemRows = rows.get("item");
            if (itemRows == null) {
                itemRows = new ArrayList<>(PAGE_SIZE);
            }
            itemRows.add(row);
            rows.put("item", itemRows);
            if (!rowSet.next()) {
                break;
            }
        }

    }

    @Override
    public Map<String, Link> getLinks() {
        Map<String, Link> links = new HashMap<>();
        links.put("current", new Link("/ryvrs/" + getTitle()));
        links.put("self", new Link("/ryvrs/" + getTitle() + "?page=" + page));
        links.put("first", new Link("/ryvrs/" + getTitle() + "?page=" + 1));
        if (page > 1) {
            links.put("prev",
                    new Link("/ryvrs/" + getTitle() + "?page=" + (page - 1l)));
        }
        if (page < pages) {
            links.put("next",
                    new Link("/ryvrs/" + getTitle() + "?page=" + (page + 1l)));
        } else {
            links.put("last",
                    new Link("/ryvrs/" + getTitle() + "?page=" + (pages)));
        }
        return links;
    }

}
