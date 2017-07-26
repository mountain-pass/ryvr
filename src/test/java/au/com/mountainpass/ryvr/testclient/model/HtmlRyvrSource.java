package au.com.mountainpass.ryvr.testclient.model;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.mountainpass.ryvr.model.Field;
import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.RyvrSource;
import au.com.mountainpass.ryvr.testclient.HtmlRyvrClient;

public class HtmlRyvrSource extends RyvrSource {
  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  WebDriver webDriver;
  public long pagePosition;
  public URI currentUri;
  public Record record = new Record() {

    @Override
    public int size() {
      return getFieldNames().length;
    }

    @Override
    public Field getField(int fieldIndex) {
      Field field = new Field() {

        private int fieldIndex;

        @Override
        public void setFieldIndex(int fieldIndex) {
          this.fieldIndex = fieldIndex;
        }

        @Override
        public Object getValue() {
          WebElement row = webDriver.findElement(By.tagName("section")).findElement(
              By.cssSelector("#items > table > tbody > tr:nth-child(" + (pagePosition + 1) + ")"));
          WebElement field = row
              .findElement(By.cssSelector("td:nth-child(" + (fieldIndex + 1) + ")"));
          String text = field.getText();
          String type = field.getAttribute("data-type");
          switch (type) {
          case "number":
            return new BigDecimal(text);
          default:
            return text;
          }
        }

        @Override
        public String getName() {
          WebElement headerRow = webDriver.findElement(By.tagName("section"))
              .findElement(By.cssSelector("#items > table > thead > tr"));
          WebElement header = headerRow
              .findElement(By.cssSelector("th:nth-child(" + (fieldIndex + 1) + ")"));
          return header.getText();
        }
      };
      field.setFieldIndex(fieldIndex);
      return field;
    }
  };

  public HtmlRyvrSource(WebDriver webDriver) {
    this.webDriver = webDriver;
  }

  @Override
  public Iterator<Record> iterator() {
    return new HtmlRyvrSourceIterator(this);
  }

  // @Override
  // public ListIterator<Record> listIterator(int position) {
  // return new RyvrIterator(position);
  // }

  public String getNextLink() {
    WebElement link = webDriver.findElement(By.tagName("section"))
        .findElement(By.cssSelector("a[rel~='" + "next" + "']"));
    String href = link.getAttribute("href");
    if (href != null && href.isEmpty()) {
      return null;
    } else {
      return href;
    }
  }

  public void followLink(String rel) {
    WebElement link = webDriver.findElement(By.tagName("section"))
        .findElement(By.cssSelector("a[rel~='" + rel + "']"));
    link.click();
    HtmlRyvrClient.waitTillLoaded(webDriver, 1000);
  }

  public void followNextLink() {
    followLink("next");
  }

  // @Override
  // public long longSize() {
  // long count = Long.parseLong(webDriver.findElement(By.id("property::count")).getText());
  // return count;
  // // long pageNo = Long.parseLong(webDriver.findElement(By.id("property::page")).getText());
  // // long pageSize =
  // Long.parseLong(webDriver.findElement(By.id("property::pageSize")).getText());
  // // long currentPageCount = webDriver.findElements(By.className("itemRow")).size() - 1;
  // // return (pageNo - 1) * pageSize + currentPageCount;
  // }

  public int getUnderlyingPageSize() {
    HtmlRyvrClient.waitTillVisible(webDriver, 1000, "header-page-size");
    return Integer.parseInt(webDriver.findElement(By.id("header-page-size")).getText());
  }

  // @Override
  // public Record get(int index) {
  // return listIterator(index).next();
  // }

  @Override
  public String[] getFieldNames() {
    List<String> headings = webDriver.findElements(By.className("itemHeading")).stream()
        .map(element -> element.getText()).collect(Collectors.toList());
    return headings.toArray(new String[] {});
  }

  @Override
  public void refresh() {
    followLink("self");
  }

  @Override
  public Iterator<Record> iterator(long position) {
    return new HtmlRyvrSourceIterator(this, position);
  }

  @Override
  public long getRecordsRemaining(long fromPosition) {
    throw new NotImplementedException("TODO");
  }

  public int getPageNo() {
    HtmlRyvrClient.waitTillVisible(webDriver, 1000, "header-page");
    return Integer.parseInt(webDriver.findElement(By.id("header-page")).getText());
  }

  public void followUri(URI resolve) throws IOException {
    webDriver.get(resolve.toString());
    HtmlRyvrClient.waitTillLoaded(webDriver, 1000);
  }

  public boolean isArchivePage() {

    // for (int i = 0; i < 5000 && !isViewLoaded(); i += 100)
    // try {
    // Thread.sleep(100);
    // } catch (InterruptedException e) {
    // // meh
    // }
    HtmlRyvrClient.waitTillVisible(webDriver, 1000, "header-archive-page");
    return Boolean.parseBoolean(webDriver.findElement(By.id("header-archive-page")).getText());
  }

  private boolean isViewLoaded() {
    if (webDriver instanceof JavascriptExecutor) {
      return (boolean) ((JavascriptExecutor) webDriver).executeScript(
          "return angular.element(document.getElementById('controller')).scope().controller.viewLoaded;");
    } else {
      return true;
    }
  }

  public long getUnderlyingCurrentPageSize() {
    return webDriver.findElement(By.tagName("section"))
        .findElements(By.cssSelector("#items > table > tbody > tr")).size();
  }

  @Override
  public boolean isLoaded(long page) {
    throw new NotImplementedException("TODO");
  }

}
