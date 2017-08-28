package au.com.mountainpass.ryvr.steps.coverage;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.com.mountainpass.ryvr.Application;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

@ContextConfiguration(classes = { Application.class })
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public class CoverageSteps {
  private File coverageXml;

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Given("^the \"([^\"]*)\" test has been run$")
  public void the_test_has_been_run(String test) throws Throwable {
    File jacocoExec = new File("build/coverage-results-" + test + "/jacoco/" + test + ".exec");
    assumeTrue(jacocoExec.canRead());
  }

  @Given("^the test coverage report has been generated$")
  public void the_test_coverage_report_has_been_generated() throws Throwable {
    coverageXml = new File("build/coverage-results/jacoco/ryvr.xml");
    assumeTrue(coverageXml.canRead());
  }

  @Then("^the \"([^\"]*)\" coverage should be at least (\\d+.?\\d*)%$")
  public void the_coverage_should_be_at_least(String type, double minPercent) throws Throwable {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = dbf.newDocumentBuilder();
    builder.setEntityResolver(new EntityResolver() {

      @Override
      public InputSource resolveEntity(String publicId, String systemId)
          throws SAXException, IOException {
        return new InputSource(new StringReader(""));
      }
    });
    Document results = builder.parse(coverageXml);
    // parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    // parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
    XPathFactory xPathfactory = XPathFactory.newInstance();
    XPath xpath = xPathfactory.newXPath();
    Double covered = (Double) xpath.evaluate("/report/counter[@type='" + type + "']/@covered",
        results, XPathConstants.NUMBER);
    Double missed = (Double) xpath.evaluate("/report/counter[@type='" + type + "']/@missed",
        results, XPathConstants.NUMBER);
    double actual = ((covered / (covered + missed)) * 100.0);
    LOGGER.info("coverage - {}: {}%", type, actual);
    assertThat(actual, greaterThan(minPercent));
  }
}
