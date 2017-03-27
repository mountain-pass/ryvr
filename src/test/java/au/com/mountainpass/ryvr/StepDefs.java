package au.com.mountainpass.ryvr;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
    private CompletableFuture<SwaggerResponse> swaggerResponseFuture;
    private CompletableFuture<RootResponse> rootResponseFuture;
    private CompletableFuture<RyvrsCollectionResponse> ryvrsCollectionResponse;

    @Autowired
    private RyvrsCollection ryvrsCollection;

    @Before
    public void setUp() {
        ryvrsCollection.clear();
    }

    @After
    public void tearDown() {
        if (db != null) {
            db.shutdown();
        }
    }

    @When("^a request is made for the API Docs$")
    public void a_request_is_made_for_the_API_Docs() throws Throwable {
        swaggerResponseFuture = client.getApiDocs();
    }

    @Then("^the API Docs will contain an operation for getting the API Docs$")
    public void the_API_Docs_will_contain_an_operation_for_getting_the_API_Docs()
            throws Throwable {
        swaggerResponseFuture.get(5, TimeUnit.SECONDS)
                .assertHasGetApiDocsOperation();
    }

    @When("^a request is made to the server's base URL$")
    public void a_request_is_made_to_the_server_s_base_URL() throws Throwable {
        rootResponseFuture = client.getRoot();
    }

    @Then("^the root entity will contain a link to the api-docs$")
    public void the_root_entity_will_contain_a_link_to_the_api_docs()
            throws Throwable {
        rootResponseFuture.get(5, TimeUnit.SECONDS).assertHasApiDocsLink();
    }

    @Then("^the root entity will contain a link to the ryvrs$")
    public void the_root_entity_will_contain_a_link_to_the_ryvrs()
            throws Throwable {
        rootResponseFuture.get(5, TimeUnit.SECONDS).assertHasRyvrsLink();
    }

    @Then("^the root entity will have an application name of \"([^\"]*)\"$")
    public void the_root_entity_will_have_an_application_name_of(
            String applicationName) throws Throwable {
        rootResponseFuture.get(5, TimeUnit.SECONDS)
                .assertHasTitle(applicationName);
    }

    @Given("^there are no ryvrs configured$")
    public void there_are_no_ryvrs_configured() throws Throwable {
        // do nothing
    }

    @When("^the ryvrs list is retrieved$")
    public void the_ryvrs_list_is_retrieved() throws Throwable {
        ryvrsCollectionResponse = client.getRyvrsCollection();
    }

    @Autowired
    private EmbeddedDatabase db;

    @Autowired
    private JdbcTemplate jt;

    private CompletableFuture<RyvrResponse> ryvrResponse;

    @Given("^a database \"([^\"]*)\"$")
    public void a_database(String dbName) throws Throwable {
        db = new EmbeddedDatabaseBuilder().setName(dbName)
                .setType(EmbeddedDatabaseType.H2).setScriptEncoding("UTF-8")
                .ignoreFailedDrops(true).addScript("initH2.sql").build();
        jt = new JdbcTemplate(db);
    }

    @Given("^it has a table \"([^\"]*)\" with the following events$")
    public void it_has_a_table_with_the_following_events(String table,
            List<Map<String, String>> events) throws Throwable {
        StringBuffer statementBuffer = new StringBuffer();
        statementBuffer.append("create table ");
        statementBuffer.append(table);
        // | ID | ACCOUNT | DESCRIPTION | AMOUNT |
        statementBuffer.append(
                " (ID INT, ACCOUNT VARCHAR, DESCRIPTION VARCHAR, AMOUNT Decimal(19,4))");

        jt.execute(statementBuffer.toString());

        jt.batchUpdate(
                "insert into " + table
                        + "(ID, ACCOUNT, DESCRIPTION, AMOUNT) values (?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i)
                            throws SQLException {
                        // TODO Auto-generated method stub
                        Map<String, String> row = events.get(i);
                        ps.setInt(1, Integer.parseInt(row.get("ID")));
                        ps.setString(2, row.get("ACCOUNT"));
                        ps.setString(3, row.get("DESCRIPTION"));
                        ps.setBigDecimal(4, new BigDecimal(row.get("AMOUNT")));
                    }

                    @Override
                    public int getBatchSize() {
                        return events.size();
                    }
                });
    }

    @Given("^a \"([^\"]*)\" ryvr for \"([^\"]*)\" for table \"([^\"]*)\"$")
    public void a_ryvr_for_for_table(String name, String dbName, String table)
            throws Throwable {
        JdbcRyvr ryvr = new JdbcRyvr(name, jt, table);
        ryvrsCollection.addRyvr(ryvr);
    }

    @Then("^the ryvrs list will contain the following entries$")
    public void the_ryvrs_list_will_contain_the_following_entries(
            List<String> names) throws Throwable {
        ryvrsCollectionResponse.get(5, TimeUnit.SECONDS)
                .assertHasEmbedded(names);
    }

    @Then("^the ryvrs list will be empty$")
    public void the_ryvrs_list_will_be_empty() throws Throwable {
        ryvrsCollectionResponse.get(5, TimeUnit.SECONDS).assertIsEmpty();
    }

    @Then("^the count of ryvrs will be (\\d+)$")
    public void the_count_of_ryvrs_will_be(int count) throws Throwable {
        ryvrsCollectionResponse.get(50, TimeUnit.SECONDS).assertCount(count);
    }

    @When("^the \"([^\"]*)\" ryvr is retrieved$")
    public void the_ryvr_is_retrieved(String name) throws Throwable {
        ryvrResponse = client.getRyvr(name);
    }

    @Then("^it will contain$")
    public void it_will_contain(List<Map<String, String>> events)
            throws Throwable {
        ryvrResponse.get(300, TimeUnit.SECONDS).assertHasEmbedded(events);
    }

    @Then("^it will have the following links$")
    public void it_will_have_the_following_links(List<String> links)
            throws Throwable {
        ryvrResponse.get(5, TimeUnit.SECONDS).assertHasLinks(links);
    }

    @Then("^it will \\*not\\* have the following links$")
    public void it_will_not_have_the_following_links(List<String> links)
            throws Throwable {
        ryvrResponse.get(5, TimeUnit.SECONDS).assertDoesntHaveLinks(links);
    }
}
