package au.com.mountainpass.ryvr.steps;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import au.com.mountainpass.ryvr.Application;
import au.com.mountainpass.ryvr.config.TestConfiguration;
import au.com.mountainpass.ryvr.testclient.RyvrTestClient;
import au.com.mountainpass.ryvr.testclient.RyvrTestDbDriver;
import au.com.mountainpass.ryvr.testclient.RyvrTestServerAdminDriver;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import cucumber.api.PendingException;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@ContextConfiguration(classes = { Application.class, TestConfiguration.class })
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class StepDefs {

    @Autowired
    private RyvrTestClient client;

    @Autowired
    private RyvrTestServerAdminDriver configClient;

    @Autowired
    private RyvrTestDbDriver dbClient;

    private RootResponse rootResponseFuture;

    private RyvrResponse ryvrResponse;

    private RyvrsCollectionResponse ryvrsCollectionResponse;

    private SwaggerResponse swaggerResponseFuture;
    private String currentTable;
    private List<Map<String, String>> currentEvents;

    public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private String databaseName;

    @Given("^a database \"([^\"]*)\"$")
    public void aDatabase(final String dbName) throws Throwable {

        dbClient.createDatabase(dbName);
        databaseName = dbName;
    }

    @When("^a request is made for the API Docs$")
    public void aRequestIsMadeForTheAPIDocs() throws Throwable {
        configClient.ensureStarted();
        swaggerResponseFuture = client.getApiDocs();
    }

    @When("^a request is made to the server's base URL$")
    public void aRequestIsMadeToTheServersBaseURL() throws Throwable {
        configClient.ensureStarted();
        rootResponseFuture = client.getRoot();
    }

    @Given("^a database ryvr with the following configuration$")
    public void a_database_ryvr_with_the_following_configuration(
            Map<String, String> config) throws Throwable {
        configClient.createDataSourceRyvr(config);

        // assertThat(ryvrsCollection.getRyvrs().keySet(),
        // hasItem(config.get("name")));
        // Ryvr ryvr = ryvrsCollection.getRyvrs().get(config.get("name"));
        // assertThat(ryvr, instanceOf(DataSourceRyvr.class));
        // DataSourceRyvr dataSourceRyvr = (DataSourceRyvr) ryvr;
        // assertThat(dataSourceRyvr.getJdbcTemplate().getDataSource()
        // .getConnection().getCatalog(), equalTo(config.get("database")));
        // assertThat(dataSourceRyvr.getPageSize(),
        // equalTo(Long.parseLong(config.get("page size"))));
        // assertThat(dataSourceRyvr.getTable(), equalTo(config.get("table")));
        // assertThat(dataSourceRyvr.getOrderedBy(),
        // equalTo(config.get("ordered by")));
        //
        // ryvrsCollection.getRyvrs().clear();
        // ryvrsCollection.getRyvrs().put(config.get("name"), ryvr);
    }

    @Given("^it has a table \"([^\"]*)\" with the following events$")
    public void itHasATableWithTheFollowingEvents(final String table,
            final List<Map<String, String>> events) throws Throwable {
        createTable(databaseName, table);

        insertRows(databaseName, table, events);
    }

    public void insertRows(final String catalog, final String table,
            final List<Map<String, String>> events) throws Throwable {
        dbClient.insertRows(catalog, table, events);

    }

    class GetTableNames implements DatabaseMetaDataCallback {

        @Override
        public Object processMetaData(DatabaseMetaData dbmd)
                throws SQLException {
            ResultSet rs = dbmd.getTables(dbmd.getUserName(), null, null,
                    new String[] { "TABLE" });
            ArrayList<String> l = new ArrayList<>();
            while (rs.next()) {
                l.add(rs.getString(3));
            }
            return l;
        }
    }

    public void createTable(String catalog, final String table)
            throws Throwable {
        dbClient.createTable(catalog, table);

    }

    @Then("^it will contain$")
    public void itWillContain(final List<Map<String, String>> events)
            throws Throwable {
        ryvrResponse.assertHasItems(events);
    }

    @Then("^it will have the following links$")
    public void itWillHaveTheFollowingLinks(final List<String> links)
            throws Throwable {
        ryvrResponse.assertHasLinks(links);
    }

    @Then("^it will \\*not\\* have the following links$")
    public void itWillNotHaveTheFollowingLinks(final List<String> links)
            throws Throwable {
        ryvrResponse.assertDoesntHaveLinks(links);
    }

    @Before
    public void _before(final Scenario scenario) {
        client.before(scenario);
        configClient._before(scenario);
    }

    @After
    public void _after(final Scenario scenario) {
        client.after(scenario);
        configClient._after(scenario);
    }

    @Then("^the API Docs will contain an operation for getting the API Docs$")
    public void theAPIDocsWillContainAnPperationForGettingTheAPIDocs()
            throws Throwable {
        swaggerResponseFuture.assertHasGetApiDocsOperation();
    }

    @Then("^the count of ryvrs will be (\\d+)$")
    public void theCountOfRyvrsWillBe(final int count) throws Throwable {
        ryvrsCollectionResponse.assertCount(count);
    }

    @Then("^the root entity will contain a link to the api-docs$")
    public void theRootEntityWillContainALinkToTheApiDocs() throws Throwable {
        rootResponseFuture.assertHasApiDocsLink();
    }

    @Then("^the root entity will contain a link to the ryvrs$")
    public void theRootEntityWillContainALinkToTheRyvrs() throws Throwable {
        rootResponseFuture.assertHasRyvrsLink();
    }

    @Then("^the root entity will have an application name of \"([^\"]*)\"$")
    public void theRootEntityWillHaveAnApplicationNameOf(
            final String applicationName) throws Throwable {
        rootResponseFuture.assertHasTitle(applicationName);
    }

    @When("^the \"([^\"]*)\" ryvr is retrieved$")
    public void theRyvrIsRetrieved(final String name) throws Throwable {
        configClient.ensureStarted();
        ryvrResponse = client.getRyvr(name);
    }

    @When("^the ryvrs list is retrieved$")
    public void theRyvrsListIsRetrieved() throws Throwable {
        configClient.ensureStarted();
        ryvrsCollectionResponse = client.getRyvrsCollection();
    }

    @Then("^the ryvrs list will be empty$")
    public void theRyvrsListWillBeEmpty() throws Throwable {
        ryvrsCollectionResponse.assertIsEmpty();
    }

    @Then("^the ryvrs list will contain the following entries$")
    public void theRyvrsListWillContainTheFollowingEntries(
            final List<String> names) throws Throwable {
        ryvrsCollectionResponse.assertHasItem(names);
    }

    @Given("^there are no ryvrs configured$")
    public void thereAreNoRyvrsConfigured() throws Throwable {
        configClient.clearRyvrs();
    }

    @When("^the previous page is requested$")
    public void thePreviousPageIsRequested() throws Throwable {
        ryvrResponse = ryvrResponse.followLink("prev");
    }

    @When("^the first page is requested$")
    public void theFirstPageIsRequested() throws Throwable {
        ryvrResponse = ryvrResponse.followLink("first");
    }

    @When("^the current page is requested$")
    public void theCurrentPageIsRequested() throws Throwable {
        ryvrResponse = ryvrResponse.followLink("current");
    }

    @When("^the next page is requested$")
    public void theNextPageIsRequested() throws Throwable {
        ryvrResponse = ryvrResponse.followLink("next");
    }

    @When("^the self link is requested$")
    public void theSelfLinkIsRequested() throws Throwable {
        ryvrResponse = ryvrResponse.followLink("self");
    }

    @Given("^it has a table \"([^\"]*)\" with the following structure$")
    public void itHasATableWithTheFollowingStructure(final String table,
            final List<String> structure) throws Throwable {
        createTable("test_db", table);
        this.currentTable = table;
    }

    @Given("^it has (\\d+) events$")
    public void itHasEvents(int noOfEvents) throws Throwable {
        List<Map<String, String>> events = new ArrayList<>(noOfEvents);
        for (int i = 0; i < noOfEvents; ++i) {
            Map<String, String> event = new HashMap<>(4);
            event.put("id", Integer.toString(i));
            event.put("account", "78901234");
            event.put("description", "Buying Stuff");
            event.put("amount", Double.toString(i * -20.00 - i));
            events.add(event);
        }
        this.currentEvents = events;
        insertRows("test_db", this.currentTable, events);
    }

    @Then("^it will have the following structure$")
    public void itWillHaveTheFollowingStructure(List<String> structure)
            throws Throwable {
        ryvrResponse.assertItemsHaveStructure(structure);
    }

    @Then("^it will have the last (\\d+) events$")
    public void itWillHaveTheLastEvents(int noOfEvents) throws Throwable {
        List<Map<String, String>> lastEvents = currentEvents.subList(
                currentEvents.size() - noOfEvents, currentEvents.size());
        ryvrResponse.assertHasItems(lastEvents);
    }

    @When("^all the events are retrieved$")
    public void all_the_events_are_retrieved() throws Throwable {
        ryvrResponse.retrieveAllEvents();
    }

    @Then("^(\\d+)% of the pages should be loaded within (\\d+)ms$")
    public void of_the_pages_should_be_loaded_within_ms(int percentile,
            int maxMs) throws Throwable {
        ryvrResponse.assertLoadedWithin(percentile, maxMs);
    }

    @Then("^(\\d+)% of the pages should be loaded successfully$")
    public void of_the_pages_should_be_loaded_successfully(int arg1)
            throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
