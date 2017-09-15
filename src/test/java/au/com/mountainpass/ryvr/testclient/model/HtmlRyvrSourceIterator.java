package au.com.mountainpass.ryvr.testclient.model;

import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;

import au.com.mountainpass.ryvr.model.Record;

class HtmlRyvrSourceIterator implements Iterator<Record> {
  /**
   * 
   */
  private final HtmlRyvrSource htmlRyvrSource;
  // long pagePosition = -1;
  long currentPage = -1;
  private long pageSize;
  private boolean isArchivePage;
  private long currentPageSize;

  public HtmlRyvrSourceIterator(HtmlRyvrSource htmlRyvrSource) {
    this(htmlRyvrSource, 0L);
  }

  public HtmlRyvrSourceIterator(HtmlRyvrSource htmlRyvrSource, long position) {
    if (position < 0) {
      throw new NoSuchElementException("No value present");
    }
    this.htmlRyvrSource = htmlRyvrSource;
    this.pageSize = htmlRyvrSource.getUnderlyingPageSize();
    long page = (position / pageSize) + 1;
    htmlRyvrSource.pagePosition = ((position - 1) % pageSize);
    int currentPage = htmlRyvrSource.getPageNo();
    if (currentPage != page) {
      URI currentUri = URI.create(htmlRyvrSource.webDriver.getCurrentUrl());
      String currentFile = currentUri.getPath();
      String newQuery = "page=" + page;
      if (page == 1) {
        htmlRyvrSource.followLink("first");
      } else {
        while (page > currentPage) {
          htmlRyvrSource.followLink("next");
          currentPage = htmlRyvrSource.getPageNo();
        }
        while (page < currentPage) {
          htmlRyvrSource.followLink("prev");
          currentPage = htmlRyvrSource.getPageNo();
        }
      }
      // LOGGER.info("getting new page: {} for position {}", page, position);
    }
    isArchivePage = htmlRyvrSource.isArchivePage();
    if (!isArchivePage) {
      currentPageSize = htmlRyvrSource.getUnderlyingCurrentPageSize();
    }
  }

  @Override
  public boolean hasNext() {
    return isArchivePage || htmlRyvrSource.pagePosition < currentPageSize - 1;
  }

  @Override
  public Record next() {
    ++htmlRyvrSource.pagePosition;
    if (htmlRyvrSource.pagePosition == pageSize) {
      htmlRyvrSource.followNextLink();
      htmlRyvrSource.pagePosition = 0;
      isArchivePage = htmlRyvrSource.isArchivePage();
      if (!isArchivePage) {
        currentPageSize = htmlRyvrSource.getUnderlyingCurrentPageSize();
      }
    }
    URI currentUrl = URI.create(htmlRyvrSource.webDriver.getCurrentUrl());
    URI newUrl = currentUrl.resolve("#" + htmlRyvrSource.pagePosition);
    htmlRyvrSource.webDriver.get(newUrl.toString());
    return this.htmlRyvrSource.record;
  }

}