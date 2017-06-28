package au.com.mountainpass.ryvr.testclient.model;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import au.com.mountainpass.ryvr.model.Field;
import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.RyvrSource;
import au.com.mountainpass.ryvr.testclient.HtmlRyvrClient;

public class HtmlRyvrSource extends RyvrSource {

  private WebDriver webDriver;

  public HtmlRyvrSource(WebDriver webDriver) {
    this.webDriver = webDriver;
  }

  private class RyvrIterator implements ListIterator<Record> {
    long pagePosition = -1;
    long currentPage = -1;

    public RyvrIterator() {
      // TODO Auto-generated constructor stub
    }

    public RyvrIterator(long position) {
      currentPage = position / getUnderlyingPageSize();
      pagePosition = position % getUnderlyingPageSize();
    }

    @Override
    public boolean hasNext() {
      if (currentPage < 0) {
        // first call to hasNext, so load the first page and check if we have records;
        followLink("first");
        currentPage = 0;
        return getUnderlyingPageSize() != 0;
      } else if (getNextLink() != null) {
        // if there is a next link, then there are definitely next records
        return true;
      } else {

        // otherwise we are on the most recent page (AKA the current page)
        // so check if there are rows after the row we are pointing to at
        // the moment.
        int recordsOnPage = getUnderlyingPageSize();
        if (pagePosition < recordsOnPage - 1) {
          return true;
        } else {
          // otehrwise, relaod and see if we have new events
          followLink("last");
          recordsOnPage = getUnderlyingPageSize();
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
      if (pagePosition == getUnderlyingPageSize()) {
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

    @Override
    public boolean hasPrevious() {
      throw new NotImplementedException("TODO");
    }

    @Override
    public Record previous() {
      throw new NotImplementedException("TODO");
    }

    @Override
    public int nextIndex() {
      throw new NotImplementedException("TODO");
    }

    @Override
    public int previousIndex() {
      throw new NotImplementedException("TODO");
    }

    @Override
    public void set(Record e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void add(Record e) {
      throw new UnsupportedOperationException();
    }

  }

  @Override
  public Iterator<Record> iterator() {
    return new RyvrIterator();
  }

  @Override
  public ListIterator<Record> listIterator(int position) {
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
  public long longSize() {
    long count = Long.parseLong(webDriver.findElement(By.id("property::count")).getText());
    return count;
    // long pageNo = Long.parseLong(webDriver.findElement(By.id("property::page")).getText());
    // long pageSize = Long.parseLong(webDriver.findElement(By.id("property::pageSize")).getText());
    // long currentPageCount = webDriver.findElements(By.className("itemRow")).size() - 1;
    // return (pageNo - 1) * pageSize + currentPageCount;
  }

  public int getUnderlyingPageSize() {
    return webDriver.findElements(By.className("itemRow")).size();
  }

  @Override
  public Record get(int index) {
    return listIterator(index).next();
  }

  @Override
  public String[] getFieldNames() {
    List<String> headings = webDriver.findElements(By.className("itemHeading")).stream()
        .map(element -> element.getText()).collect(Collectors.toList());
    return headings.toArray(new String[] {});
  }

}
