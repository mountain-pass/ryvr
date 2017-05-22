package au.com.mountainpass.ryvr.testclient;

import java.util.Map;

import cucumber.api.Scenario;

public interface RyvrTestServerAdminDriver {

    void createDataSourceRyvr(Map<String, String> config) throws Throwable;

    void _before(Scenario scenario);

    void clearRyvrs();

    void ensureStarted() throws Throwable;

    void _after(Scenario scenario);

}
