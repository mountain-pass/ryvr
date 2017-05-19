package au.com.mountainpass.ryvr.testclient;

import java.util.List;
import java.util.Map;

import cucumber.api.Scenario;

public interface RyvrTestConfigClient {

    void createDatabase(String dbName) throws Throwable;

    void createDataSourceRyvr(Map<String, String> config) throws Throwable;

    void insertRows(String catalog, String table,
            List<Map<String, String>> events) throws Throwable;

    void createTable(String catalog, String table) throws Throwable;

    void _before(Scenario scenario);

    void clearRyvrs();

    void ensureStarted() throws Throwable;

    void _after(Scenario scenario);

}
