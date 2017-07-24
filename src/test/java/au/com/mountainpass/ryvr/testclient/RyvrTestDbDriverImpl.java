package au.com.mountainpass.ryvr.testclient;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class RyvrTestDbDriverImpl implements RyvrTestDbDriver {
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private JdbcTemplate currentJt;

  @Override
  public void createDatabase(final String dbName) throws Throwable {
    final Connection connection = currentJt.getDataSource().getConnection();
    final String identifierQuoteString = connection.getMetaData().getIdentifierQuoteString();

    final String dbProductName = connection.getMetaData().getDatabaseProductName();
    LOGGER.info("dbProductName: {}", dbProductName);
    switch (dbProductName) {
    case "H2":
    case "MySQL":
      currentJt.execute(
          "DROP SCHEMA IF EXISTS " + identifierQuoteString + dbName + identifierQuoteString + " ;");
      break;
    case "PostgreSQL":
      currentJt.execute("DROP SCHEMA IF EXISTS " + identifierQuoteString + dbName
          + identifierQuoteString + " CASCADE;");
      break;
    default:
      throw new NotImplementedException(dbProductName);
    }
    currentJt
        .execute("CREATE SCHEMA " + identifierQuoteString + dbName + identifierQuoteString + ";");

    connection.setCatalog(dbName);
    connection.close();
  }

  @Override
  public void createTable(final String catalog, final String table) throws Throwable {
    final Connection connection = currentJt.getDataSource().getConnection();
    final String identifierQuoteString = connection.getMetaData().getIdentifierQuoteString();
    final String catalogSeparator = connection.getMetaData().getCatalogSeparator();
    currentJt.execute("drop table if exists " + identifierQuoteString + catalog
        + identifierQuoteString + catalogSeparator + identifierQuoteString + table
        + identifierQuoteString + " CASCADE");

    final StringBuilder statementBuffer = new StringBuilder();
    statementBuffer.append("create table ");
    statementBuffer
        .append(identifierQuoteString + catalog + identifierQuoteString + catalogSeparator);
    statementBuffer.append(identifierQuoteString + table + identifierQuoteString);
    statementBuffer.append(" (" + identifierQuoteString + "id" + identifierQuoteString
        + " INT UNSIGNED, " + identifierQuoteString + "account" + identifierQuoteString
        + " VARCHAR(255), " + identifierQuoteString + "description" + identifierQuoteString
        + " VARCHAR(255), " + identifierQuoteString + "amount" + identifierQuoteString
        + " Decimal(19,4), CONSTRAINT " + identifierQuoteString + "pk_id" + identifierQuoteString
        + " PRIMARY KEY (" + identifierQuoteString + "id" + identifierQuoteString + "))");
    LOGGER.info("TABLE: {}", statementBuffer.toString());
    currentJt.execute(statementBuffer.toString());
    currentJt.update("DELETE FROM " + identifierQuoteString + catalog + identifierQuoteString
        + catalogSeparator + identifierQuoteString + table + identifierQuoteString);
    connection.close();
  }

  @Override
  public void insertRows(final String catalog, final String table,
      final List<Map<String, String>> events) throws Throwable {
    final Connection connection = currentJt.getDataSource().getConnection();
    final String identifierQuoteString = connection.getMetaData().getIdentifierQuoteString();
    final String catalogSeparator = connection.getMetaData().getCatalogSeparator();

    currentJt.batchUpdate("insert into " + identifierQuoteString + catalog + identifierQuoteString
        + catalogSeparator + identifierQuoteString + table + identifierQuoteString + "("
        + identifierQuoteString + "id" + identifierQuoteString + ", " + identifierQuoteString
        + "account" + identifierQuoteString + ", " + identifierQuoteString + "description"
        + identifierQuoteString + ", " + identifierQuoteString + "amount" + identifierQuoteString
        + ") values (?, ?, ?, ?)", new BatchPreparedStatementSetter() {

          @Override
          public int getBatchSize() {
            return events.size();
          }

          @Override
          public void setValues(final PreparedStatement ps, final int i) throws SQLException {
            // TODO Auto-generated method stub
            final Map<String, String> row = events.get(i);
            ps.setInt(1, Integer.parseInt(row.get("id")));
            ps.setString(2, row.get("account"));
            ps.setString(3, row.get("description"));
            ps.setBigDecimal(4, new BigDecimal(row.get("amount")));
          }

        });
    // currentJt.execute("OPTIMIZE TABLE " + identifierQuoteString + catalog + identifierQuoteString
    // + catalogSeparator + identifierQuoteString + table + identifierQuoteString);

    connection.close();
  }

}
