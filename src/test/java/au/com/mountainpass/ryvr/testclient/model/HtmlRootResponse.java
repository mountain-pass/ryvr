package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import au.com.mountainpass.ryvr.testclient.HtmlRyvrClient;

public class HtmlRootResponse implements RootResponse {

    private WebDriver webDriver;

    public HtmlRootResponse(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    @Override
    public void assertHasApiDocsLink() {
        HtmlRyvrClient.waitTillLoaded(webDriver, 5);
        List<String> linkTitles = getLinkTitles();
        assertThat(linkTitles, hasItem("API Docs"));
    }

    private List<String> getLinkTitles() {
        List<WebElement> links = webDriver.findElement(By.id("links"))
                .findElements(By.tagName("a"));
        List<String> linkTitles = links.stream().map(element -> {
            return element.getText();
        }).collect(Collectors.toList());
        return linkTitles;
    }

    @Override
    public void assertHasRyvrsLink() {
        HtmlRyvrClient.waitTillLoaded(webDriver, 5);
        List<String> linkTitles = getLinkTitles();
        assertThat(linkTitles, hasItem("Ryvrs"));
    }

    @Override
    public void assertHasTitle(String title) {
        HtmlRyvrClient.waitTillLoaded(webDriver, 5);
        WebElement menuBarTitle = webDriver
                .findElement(By.className("navbar-header"))
                .findElement(By.className("title"));
        assertThat(menuBarTitle.getText(), equalTo("ryvr"));
    }

}
