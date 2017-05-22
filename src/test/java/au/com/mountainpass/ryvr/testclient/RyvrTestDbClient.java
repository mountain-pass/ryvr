package au.com.mountainpass.ryvr.testclient;

import java.util.List;
import java.util.Map;

public interface RyvrTestDbClient {

    void createDatabase(String dbName) throws Throwable;

    void insertRows(String catalog, String table,
            List<Map<String, String>> events) throws Throwable;

    void createTable(String catalog, String table) throws Throwable;

}