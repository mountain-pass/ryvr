package au.com.mountainpass.ryvr.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import au.com.mountainpass.ryvr.model.Link;
import au.com.mountainpass.ryvr.model.Ryvr;

public class JdbcRyvr extends Ryvr {

    private long pageSize = 10; // as of 2017/05/02, optimal page size is
    // 2048;
    private JdbcTemplate jt;
    private SqlRowSet rowSet;
    private String[] columnNames;
    private String countQuery;
    private String rowsQuery;

    public JdbcRyvr(String title, JdbcTemplate jt, String table,
            String orderedBy, long pageSize) {
        super(title);
        this.jt = jt;
        this.pageSize = pageSize;
        countQuery = "select count(*) from \"" + table + "\"";
        rowsQuery = "select * from \"" + table + "\" ORDER BY \"" + orderedBy
                + "\" ASC";
        refresh();
    }

    @Override
    public void refresh() {
        refreshPage(-1l);
    }

    @JsonRawValue
    @JsonProperty("_embedded")
    public String getRawEmbedded() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ \"item\": [");
        for (int i = 0; i < pageSize; ++i) {
            builder.append("{");
            for (int j = 0; j < columnNames.length; ++j) {
                builder.append("\"");
                builder.append(columnNames[j]);
                builder.append("\": ");
                Object value = rowSet.getObject(j + 1);
                if (value instanceof String) {
                    builder.append("\"");
                    builder.append(value);
                    builder.append("\"");
                } else {
                    builder.append(value);
                }
                if (j + 1 < columnNames.length) {
                    builder.append(",");
                }
            }
            builder.append("}");
            if (!rowSet.next()) {
                break;
            }
            if (i + 1 < pageSize) {
                builder.append(",");
            }
        }
        builder.append("]}");
        return builder.toString();
    }

    @JsonProperty("_links")
    @JsonRawValue
    public String getRawLinks() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");

        builder.append("\"self\": { \"href\": \"/ryvrs/");
        builder.append(getTitle());
        builder.append("?page=");
        builder.append(page);
        builder.append("\"},");

        builder.append("\"first\": { \"href\": \"/ryvrs/");
        builder.append(getTitle());
        builder.append("?page=");
        builder.append(1);
        builder.append("\"},");

        if (page > 1) {
            builder.append("\"prev\": { \"href\": \"/ryvrs/");
            builder.append(getTitle());
            builder.append("?page=");
            builder.append(page - 1l);
            builder.append("\"},");
        }
        if (page < pages) {
            builder.append("\"next\": { \"href\": \"/ryvrs/");
            builder.append(getTitle());
            builder.append("?page=");
            builder.append(page + 1l);
            builder.append("\"},");
        } else {
            builder.append("\"last\": { \"href\": \"/ryvrs/");
            builder.append(getTitle());
            builder.append("?page=");
            builder.append(pages);
            builder.append("\"},");
        }
        builder.append("\"current\": { \"href\": \"/ryvrs/");
        builder.append(getTitle());
        builder.append("\"}}");
        return builder.toString();
    }

    @Override
    public void refreshPage(long requestedPage) {
        if (pages < 0l || requestedPage < 0l || requestedPage >= pages) {
            long count = jt.queryForObject(countQuery, Long.class);
            pages = ((count - 1l) / pageSize) + 1l;
        }
        if (requestedPage > pages) {
            throw new IndexOutOfBoundsException();
        }

        page = requestedPage < 0l ? pages : requestedPage;

        if (rowSet == null || page == pages) {
            jt.setFetchSize((int) pageSize);
            rowSet = jt.queryForRowSet(rowsQuery);
            columnNames = rowSet.getMetaData().getColumnNames();
        }
        // hmmm... what happens when there are more rows than MAX_INT?
        rowSet.absolute((int) ((page - 1l) * pageSize + 1l));

        // rows.clear();
        // for (int i = 0; i < PAGE_SIZE; ++i) {
        // Map<String, Object> row = new HashMap<>();
        // for (int j = columnNames.length; j > 0; --j) {
        // row.put(columnNames[j - 1], rowSet.getObject(j));
        // }
        // List<Map<String, Object>> itemRows = rows.get("item");
        // if (itemRows == null) {
        // itemRows = new ArrayList<>(PAGE_SIZE);
        // }
        // itemRows.add(row);
        // rows.put("item", itemRows);
        // if (!rowSet.next()) {
        // break;
        // }
        // }

    }

    @Override
    @JsonIgnore
    public Map<String, List<Map<String, Object>>> getEmbedded() {
        Map<String, List<Map<String, Object>>> rows = new HashMap<>();
        rowSet.absolute((int) ((page - 1l) * pageSize + 1l));
        for (int i = 0; i < pageSize; ++i) {
            Map<String, Object> row = new HashMap<>();
            for (int j = columnNames.length; j > 0; --j) {
                row.put(columnNames[j - 1], rowSet.getObject(j));
            }
            List<Map<String, Object>> itemRows = rows.get("item");
            if (itemRows == null) {
                itemRows = new ArrayList<Map<String, Object>>((int) pageSize);
            }
            itemRows.add(row);
            rows.put("item", itemRows);
            if (!rowSet.next()) {
                break;
            }
        }
        return rows;
    }

    @JsonIgnore
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

    @JsonIgnore
    public SqlRowSet getRowSet() {
        return rowSet;
    }

    @JsonIgnore
    public String[] getColumnNames() {
        return this.columnNames;
    }

}
