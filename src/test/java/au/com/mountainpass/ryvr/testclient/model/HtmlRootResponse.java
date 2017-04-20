package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import au.com.mountainpass.ryvr.testclient.HtmlRyvrClient;

public class HtmlRootResponse implements RootResponse {

    private WebDriver webDriver;

    public HtmlRootResponse(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    @Override
    public void assertHasApiDocsLink() {
        List<String> linkTitles = getLinkTitles();
        assertThat(linkTitles, hasItem("API Docs"));
    }

    private List<String> getLinkTitles() {
        List<WebElement> linksList = HtmlRyvrClient.getLinks(webDriver);
        List<String> linkTitles = linksList.stream().map(element -> {
            return element.getText();
        }).collect(Collectors.toList());
        return linkTitles;
    }

    @Override
    public void assertHasRyvrsLink() {
        List<String> linkTitles = getLinkTitles();
        assertThat(linkTitles, hasItem("Ryvrs"));
    }

    @Override
    public void assertHasTitle(String title) {
        WebElement titleElement = webDriver
                .findElement(By.className("jumbotron"))
                .findElement(By.className("title"));
        assertThat(titleElement.getText(), equalTo("ryvr"));
    }

    @Override
    public RyvrsCollectionResponse followRyvrsLink() {
        HtmlRyvrClient.waitTillLoaded(webDriver, 5);
        List<WebElement> links = HtmlRyvrClient.getLinks(webDriver);
        (new WebDriverWait(webDriver, 5))
                .until(ExpectedConditions.visibilityOfAllElements(links));
        assertHasRyvrsLink();
        for (WebElement element : links) {
            if ("Ryvrs".equals(element.getText())) {
                element.click();
                HtmlRyvrClient.waitTillLoaded(webDriver, 5);
                return new HtmlRyvrsCollectionResponse(webDriver);
            }
        }
        assertTrue(false);
        return null;
    }

}
