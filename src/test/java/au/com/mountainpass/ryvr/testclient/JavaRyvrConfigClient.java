package au.com.mountainpass.ryvr.testclient;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import au.com.mountainpass.ryvr.datasource.DataSourceRyvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import cucumber.api.Scenario;

@Component
@Profile(value = { "integrationTest" })
public class JavaRyvrConfigClient implements RyvrTestConfigClient {
    public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JdbcTemplate currentJt;

    @Autowired
    private RyvrsCollection ryvrsCollection;

    @Override
    public void createDatabase(String dbName) throws Throwable {
        Connection connection = currentJt.getDataSource().getConnection();
        String identifierQuoteString = connection.getMetaData()
                .getIdentifierQuoteString();

        String dbProductName = connection.getMetaData()
                .getDatabaseProductName();
        LOGGER.info("dbProductName: {}", dbProductName);
        switch (dbProductName) {
        case "H2":
            currentJt.execute(
                    "CREATE SCHEMA IF NOT EXISTS " + identifierQuoteString
                            + dbName + identifierQuoteString + ";");
            break;
        case "MySQL":
            currentJt.execute(
                    "CREATE DATABASE IF NOT EXISTS " + identifierQuoteString
                            + dbName + identifierQuoteString + ";");
            break;
        }
        connection.setCatalog(dbName);
        connection.close();
    }

    @Override
    public void createDataSourceRyvr(Map<String, String> config)
            throws Throwable {
        final DataSourceRyvr ryvr = new DataSourceRyvr(config.get("name"),
                currentJt, config.get("database"), config.get("table"),
                config.get("ordered by"),
                Long.parseLong(config.get("page size")));
        ryvrsCollection.addRyvr(ryvr);

    }

    @Override
    public void insertRows(String catalog, String table,
            List<Map<String, String>> events) throws Throwable {
        Connection connection = currentJt.getDataSource().getConnection();
        String identifierQuoteString = connection.getMetaData()
                .getIdentifierQuoteString();
        String catalogSeparator = connection.getMetaData()
                .getCatalogSeparator();

        currentJt.batchUpdate(
                "insert into " + identifierQuoteString + catalog
                        + identifierQuoteString + catalogSeparator
                        + identifierQuoteString + table + identifierQuoteString
                        + "(ID, ACCOUNT, DESCRIPTION, AMOUNT) values (?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public int getBatchSize() {
                        return events.size();
                    }

                    @Override
                    public void setValues(final PreparedStatement ps,
                            final int i) throws SQLException {
                        // TODO Auto-generated method stub
                        final Map<String, String> row = events.get(i);
                        ps.setInt(1, Integer.parseInt(row.get("ID")));
                        ps.setString(2, row.get("ACCOUNT"));
                        ps.setString(3, row.get("DESCRIPTION"));
                        ps.setBigDecimal(4, new BigDecimal(row.get("AMOUNT")));
                    }

                });
        connection.close();
    }

    @Override
    public void createTable(String catalog, String table) throws Throwable {
        Connection connection = currentJt.getDataSource().getConnection();
        String identifierQuoteString = connection.getMetaData()
                .getIdentifierQuoteString();
        String catalogSeparator = connection.getMetaData()
                .getCatalogSeparator();
        currentJt.execute("drop table if exists " + identifierQuoteString
                + catalog + identifierQuoteString + catalogSeparator
                + identifierQuoteString + table + identifierQuoteString);

        final StringBuilder statementBuffer = new StringBuilder();
        statementBuffer.append("create table ");
        statementBuffer.append(identifierQuoteString + catalog
                + identifierQuoteString + catalogSeparator);
        statementBuffer
                .append(identifierQuoteString + table + identifierQuoteString);
        statementBuffer.append(
                " (ID INT, ACCOUNT VARCHAR(255), DESCRIPTION VARCHAR(255), AMOUNT Decimal(19,4), CONSTRAINT PK_ID PRIMARY KEY (ID))");
        currentJt.execute(statementBuffer.toString());
        currentJt.update("DELETE FROM " + identifierQuoteString + catalog
                + identifierQuoteString + catalogSeparator
                + identifierQuoteString + table + identifierQuoteString);
        DatabaseMetaData md = connection.getMetaData();
        ResultSet rs = md.getTables(null, null, "%", null);
        while (rs.next()) {
            LOGGER.debug("TABLE: {}", rs.getString(3));
        }
        connection.close();
    }

    @Override
    public void _before(Scenario scenario) {
        clearRyvrs();
    }

    @Override
    public void clearRyvrs() {
        ryvrsCollection.clear();
    }

    @Override
    public void ensureStarted() {
        // nothing
    }

    @Override
    public void _after(Scenario scenario) {
        // nothing
    }

}
