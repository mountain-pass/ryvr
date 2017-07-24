package au.com.mountainpass.ryvr.testclient.model;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

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
    this.htmlRyvrSource = htmlRyvrSource;
    this.pageSize = htmlRyvrSource.getUnderlyingPageSize();
    long page = (position / pageSize) + 1;
    htmlRyvrSource.pagePosition = ((position - 1) % pageSize);
    int currentPage = htmlRyvrSource.getPageNo();
    if (currentPage != page) {
      URI currentUri = URI.create(htmlRyvrSource.webDriver.getCurrentUrl());
      String currentFile = currentUri.getPath();
      String newQuery = "page=" + page;
      try {
        // LOGGER.info("getting new page: {} for position {}", page, position);
        htmlRyvrSource.followUri(currentUri.resolve(currentFile + "?" + newQuery));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
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
    return this.htmlRyvrSource.record;
  }

}