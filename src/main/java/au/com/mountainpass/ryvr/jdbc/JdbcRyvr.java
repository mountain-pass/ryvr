package au.com.mountainpass.ryvr.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;

import au.com.mountainpass.ryvr.model.Ryvr;

public class JdbcRyvr extends Ryvr {

    private String table;
    private JdbcTemplate jt;
    private String name;

    public JdbcRyvr(String title, JdbcTemplate jt, String table) {
        super(title);
        this.jt = jt;
        this.table = table;
    }

}
