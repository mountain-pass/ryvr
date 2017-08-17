package au.com.mountainpass.ryvr.testclient;

import java.util.List;
import java.util.Map;

public interface RyvrTestDbDriver {

  public String getCatalog(String dbName) throws Throwable;

  public void insertRows(String catalog, String table, List<Map<String, String>> events)
      throws Throwable;

  public void createTable(String catalog, String table, Map<String, String> structure)
      throws Throwable;

  public void insertRow(String string, String currentTable, Map<String, String> event)
      throws Throwable;

  public Map<String, String> adjustConfig(Map<String, String> config) throws Throwable;

}