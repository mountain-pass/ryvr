package au.com.mountainpass.ryvr;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import au.com.mountainpass.inflector.springboot.InflectorApplication;
import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.jdbc.JdbcRyvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import au.com.mountainpass.ryvr.testclient.RyvrTestClient;
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

@ComponentScan("au.com.mountainpass")
@ContextConfiguration(classes = { InflectorApplication.class,
        RyvrConfiguration.class })
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class StepDefs {
    @Autowired
    private RyvrTestClient client;
    @Autowired
    private EmbeddedDatabase db;
    @Autowired
    private JdbcTemplate jt;
    private RootResponse rootResponseFuture;

    private RyvrResponse ryvrResponse;

    @Autowired
    private RyvrsCollection ryvrsCollection;

    private RyvrsCollectionResponse ryvrsCollectionResponse;

    private SwaggerResponse swaggerResponseFuture;
    private String currentTable;
    private List<Map<String, String>> currentEvents;

    @Given("^a database \"([^\"]*)\"$")
    public void aDatabase(final String dbName) throws Throwable {
        db = new EmbeddedDatabaseBuilder().setName(dbName)
                .setType(EmbeddedDatabaseType.H2).setScriptEncoding("UTF-8")
                .ignoreFailedDrops(true).addScript("initH2.sql").build();
        jt = new JdbcTemplate(db);
    }

    @When("^a request is made for the API Docs$")
    public void aRequestIsMadeForTheAPIDocs() throws Throwable {
        swaggerResponseFuture = client.getApiDocs();
    }

    @When("^a request is made to the server's base URL$")
    public void aRequestIsMadeToTheServersBaseURL() throws Throwable {
        rootResponseFuture = client.getRoot();
    }

    @Given("^a database ryvr with the following configuration$")
    public void a_database_ryvr_with_the_following_configuration(
            Map<String, String> config) throws Throwable {
        final JdbcRyvr ryvr = new JdbcRyvr(config.get("name"), jt,
                config.get("table"), config.get("ordered by"),
                Long.parseLong(config.get("page size")));
        ryvrsCollection.addRyvr(ryvr);
    }

    @Given("^it has a table \"([^\"]*)\" with the following events$")
    public void itHasATableWithTheFollowingEvents(final String table,
            final List<Map<String, String>> events) throws Throwable {
        createTable(table);

        insertRows(table, events);
    }

    public void insertRows(final String table,
            final List<Map<String, String>> events) {
        jt.batchUpdate(
                "insert into " + table
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
    }

    public void createTable(final String name) {
        final StringBuffer statementBuffer = new StringBuffer();
        statementBuffer.append("create table ");
        statementBuffer.append(name);
        // | ID | ACCOUNT | DESCRIPTION | AMOUNT |
        statementBuffer.append(
                " (ID INT, ACCOUNT VARCHAR, DESCRIPTION VARCHAR, AMOUNT Decimal(19,4), CONSTRAINT PK_ID PRIMARY KEY (ID))");
        jt.execute(statementBuffer.toString());
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
    public void setUp(final Scenario scenario) {
        client.before(scenario);
        ryvrsCollection.clear();
    }

    @After
    public void tearDown(final Scenario scenario) {
        client.after(scenario);
        if (db != null) {
            db.shutdown();
        }
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
        ryvrResponse = client.getRyvr(name);
    }

    @When("^the ryvrs list is retrieved$")
    public void theRyvrsListIsRetrieved() throws Throwable {
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
        ryvrsCollection.clear();
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
        createTable(table);
        this.currentTable = table;
    }

    @Given("^it has (\\d+) events$")
    public void itHasEvents(int noOfEvents) throws Throwable {
        List<Map<String, String>> events = new ArrayList<>(noOfEvents);
        for (int i = 0; i < noOfEvents; ++i) {
            Map<String, String> event = new HashMap<>(4);
            event.put("ID", Integer.toString(i));
            event.put("ACCOUNT", "78901234");
            event.put("DESCRIPTION", "Buying Stuff");
            event.put("AMOUNT", Double.toString(i * -20.00 - i));
            events.add(event);
        }
        this.currentEvents = events;
        insertRows(this.currentTable, events);
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
