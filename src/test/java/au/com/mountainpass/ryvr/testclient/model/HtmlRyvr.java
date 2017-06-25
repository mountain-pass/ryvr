package au.com.mountainpass.ryvr.testclient.model;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import au.com.mountainpass.ryvr.model.Field;
import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.testclient.HtmlRyvrClient;

public class HtmlRyvr extends Ryvr {

  private WebDriver webDriver;

  public HtmlRyvr(String title, WebDriver webDriver) {
    super(title);
    this.webDriver = webDriver;
  }

  private class RyvrIterator implements Iterator<Record> {
    long pagePosition = -1;
    long currentPage = -1;

    public RyvrIterator() {
      // TODO Auto-generated constructor stub
    }

    public RyvrIterator(long position) {
      currentPage = position / getPageSize();
      pagePosition = position % getPageSize();
    }

    @Override
    public boolean hasNext() {
      if (currentPage < 0) {
        // first call to hasNext, so load the first page and check if we have records;
        followLink("first");
        currentPage = 0;
        return getPageSize() != 0;
      } else if (getNextLink() != null) {
        // if there is a next link, then there are definitely next records
        return true;
      } else {

        // otherwise we are on the most recent page (AKA the current page)
        // so check if there are rows after the row we are pointing to at
        // the moment.
        int recordsOnPage = getPageSize();
        if (pagePosition < recordsOnPage - 1) {
          return true;
        } else {
          // otehrwise, relaod and see if we have new events
          followLink("last");
          recordsOnPage = getPageSize();
        }
        return pagePosition < recordsOnPage - 1;
      }
    }

    @Override
    public Record next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      final Record rval = new Record() {

        @Override
        public int size() {
          return getFieldNames().length;
        }

        @Override
        public Field getField(int fieldIndex) {
          final Field field = new Field() {

            private int fieldIndex;

            @Override
            public Object getValue() {
              WebElement element = webDriver
                  .findElement(By.cssSelector("#items > table > tbody > tr:nth-child("
                      + (pagePosition + 1) + ") > td:nth-child(" + (fieldIndex + 1) + ")"));
              String textValue = element.getText();
              String type = element.getAttribute("data-type");
              switch (type) {
              case "number":
                BigDecimal number = new BigDecimal(textValue);
                try {
                  return number.longValueExact();
                } catch (ArithmeticException e) {
                  return number;
                }
              case "boolean":
                return Boolean.parseBoolean(textValue);
              default:
                return textValue;
              }
            }

            @Override
            public String getName() {
              return getFieldNames()[fieldIndex];
            }

            @Override
            public void setFieldIndex(int fieldIndex) {
              this.fieldIndex = fieldIndex;
            }
          };
          field.setFieldIndex(fieldIndex);
          return field;
        }

        @Override
        public void setPosition(long pp) {
          pagePosition = (int) pp;
        }
      };
      ++pagePosition;
      if (pagePosition == getPageSize()) {
        followNextLink();
        this.pagePosition = 0;
      }
      return rval;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(Consumer<? super Record> consumer) {
      if (!hasNext()) {
        return;
      }
      while (hasNext()) {
        consumer.accept(next());
      }
    }

  }

  @Override
  public Iterator<Record> iterator() {
    return new RyvrIterator();
  }

  @Override
  public Iterator<Record> iterator(long position) {
    return new RyvrIterator(position);
  }

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
    HtmlRyvrClient.waitTillLoaded(webDriver, 5);
  }

  public void followNextLink() {
    followLink("next");
  }

  @Override
  public long getCount() {
    long count = Long.parseLong(webDriver.findElement(By.id("property::count")).getText());
    return count;
    // long pageNo = Long.parseLong(webDriver.findElement(By.id("property::page")).getText());
    // long pageSize = Long.parseLong(webDriver.findElement(By.id("property::pageSize")).getText());
    // long currentPageCount = webDriver.findElements(By.className("itemRow")).size() - 1;
    // return (pageNo - 1) * pageSize + currentPageCount;
  }

  @Override
  public int getPageSize() {
    return webDriver.findElements(By.className("itemRow")).size();
  }

  @Override
  public String[] getFieldNames() {
    List<String> headings = webDriver.findElements(By.className("itemHeading")).stream()
        .map(element -> element.getText()).collect(Collectors.toList());
    return headings.toArray(new String[] {});
  }

  // @Override
  // public void assertHasItems(List<Map<String, String>> events) {
  // HtmlRyvrClient.waitTillLoaded(webDriver, 5);
  // List<WebElement> items = webDriver.findElement(By.id("items"))
  // .findElements(By.className("itemRow"));
  //
  // List<Map<String, Object>> itemValues = items.stream().map(itemRow -> {
  // Map<String, Object> rval = new HashMap<>();
  //
  // List<WebElement> fields = itemRow.findElements(By.tagName("td"));
  // for (WebElement field : fields) {
  // String heading = field.getAttribute("data-heading");
  // String value = field.getText();
  // String type = field.getAttribute("data-type");
  // if ("number".equals(type)) {
  // rval.put(heading, new BigDecimal(value));
  // } else {
  // rval.put(heading, value);
  // }
  // }
  //
  // return rval;
  // }).collect(Collectors.toList());
  //
  // for (int i = 0; i < itemValues.size(); ++i) {
  // final Map<String, String> expectedRow = events.get(i);
  // itemValues.get(i).entrySet().forEach(entry -> {
  //
  // Object actualValue = entry.getValue();
  //
  // String expectedValue = expectedRow.get(entry.getKey());
  // Util.assertEqual(actualValue, expectedValue);
  // });
  // }
  // assertThat(items.size(), equalTo(events.size()));
  // }
  //
  // @Override
  // public void assertHasLinks(List<String> links) {
  // links.forEach(rel -> {
  // webDriver.findElement(By.cssSelector("a[rel~='" + rel + "']"));
  // });
  // }
  //
  // @Override
  // public void assertDoesntHaveLinks(List<String> links) {
  // links.forEach(rel -> {
  // assertThat(webDriver.findElements(By.cssSelector("a[rel~='" + rel + "']")), empty());
  // });
  // }

  // @Override
  // public RestRyvr followLink(String rel) {
  //
  // WebElement link = webDriver.findElement(By.tagName("section"))
  // .findElement(By.cssSelector("a[rel~='" + rel + "']"));
  // link.click();
  // HtmlRyvrClient.waitTillLoaded(webDriver, 5);
  // return new HtmlRyvr(webDriver);
  //
  // }

  // @Override
  // public void assertItemsHaveStructure(List<String> structure) {
  // HtmlRyvrClient.waitTillLoaded(webDriver, 5);
  // List<String> headings = webDriver.findElements(By.className("itemHeading")).stream()
  // .map(element -> element.getText()).collect(Collectors.toList());
  // assertThat(headings, containsInAnyOrder(structure.toArray()));
  // }
  //
  // @Override
  // public void retrieveAllEvents() {
  // throw new PendingException();
  // }
  //
  // @Override
  // public boolean hasLink(String rel) {
  // throw new PendingException();
  //
  // }
  //
  // @Override
  // public void assertLoadedWithin(int percentile, int maxMs) {
  // throw new PendingException();
  // }
  //
  // @Override
  // public void clearMetrics() {
  // throw new PendingException();
  // }
  //
  // @Override
  // public void assertFromCache() {
  // // TODO: add something to the page to indicate if the response was
  // // cached or not
  // throw new PendingException();
  // }
  //
  // @Override
  // public void assertNotFromCache() {
  // // TODO: add something to the page to indicate if the response was
  // // cached or not
  // throw new PendingException();
  // }

}
