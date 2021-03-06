package au.com.mountainpass.ryvr.rest;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.mountainpass.ryvr.model.Record;

class RestRyvrSourceIterator implements Iterator<Record> {
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  /**
   * 
   */
  private final RestRyvrSource restRyvrSource;
  private final long pageSize;
  private boolean isArchivePage;
  private long currentPageSize;

  public RestRyvrSourceIterator(RestRyvrSource restRyvrSource, long position) {
    if (position < 0) {
      throw new NoSuchElementException("No value present");
    }
    long normalizedPostion = position - 1;
    this.restRyvrSource = restRyvrSource;
    this.pageSize = restRyvrSource.getUnderlyingPageSize();
    long page = (normalizedPostion / pageSize) + 1;
    restRyvrSource.pagePosition = normalizedPostion % pageSize;
    int currentPage = restRyvrSource.getPageNo();
    if (currentPage != page) {
      String currentFile = restRyvrSource.currentUri.getPath();
      String newQuery = "page=" + page;
      try {
        // LOGGER.info("getting new page: {} for position {}", page, position);
        // TODO: use provided Link-Template
        restRyvrSource.followUri(restRyvrSource.currentUri.resolve(currentFile + "?" + newQuery));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    isArchivePage = restRyvrSource.isArchivePage();
    if (!isArchivePage) {
      currentPageSize = restRyvrSource.getUnderlyingCurrentPageSize();
    }
  }

  public RestRyvrSourceIterator(RestRyvrSource restRyvrSource) {
    this(restRyvrSource, 0L);
  }

  @Override
  public boolean hasNext() {
    // LOGGER.info("restRyvrSource.pagePosition: {}", restRyvrSource.pagePosition);
    // LOGGER.info("currentPageSize: {}", currentPageSize);
    // LOGGER.info("restRyvrSource.pagePosition < currentPageSize - 1: {}",
    // restRyvrSource.pagePosition < currentPageSize - 1);
    // LOGGER.info("isArchivePage: {}", isArchivePage);
    return isArchivePage || restRyvrSource.pagePosition < currentPageSize - 1;
    // if (isArchivePage) {
    // return true;
    // } else {
    // // long currentRecordCount = restRyvrSource.getUnderlyingCurrentPageSize();
    // return restRyvrSource.pagePosition < currentPageSize - 1;
    // }
  }

  @Override
  public Record next() {
    ++restRyvrSource.pagePosition;
    if (restRyvrSource.pagePosition == pageSize) {
      restRyvrSource.followNextLink();
      restRyvrSource.pagePosition = 0;
      isArchivePage = restRyvrSource.isArchivePage();
      if (!isArchivePage) {
        currentPageSize = restRyvrSource.getUnderlyingCurrentPageSize();
      }
    }
    return this.restRyvrSource.record;
  }
}