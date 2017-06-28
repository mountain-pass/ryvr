package au.com.mountainpass.ryvr.model;

import java.util.Iterator;

public class Ryvr implements Iterable<Record> {

  private String title;
  private int pageSize;
  RyvrSource source;

  public Ryvr(String title, int pageSize, RyvrSource source) {
    this.title = title;
    this.pageSize = pageSize;
    this.source = source;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public Iterator<Record> iterator() {
    return source.iterator();
  }

  public Iterator<Record> iterator(long position) {
    return source.listIterator(position);
  }

  public int getPageSize() {
    return this.pageSize;
  }

  public long getPages() {
    return (source.longSize() / this.pageSize) + 1;
  }

  public int getCurrentPageSize(long page) {
    long count = source.longSize();
    long pages = (count / this.pageSize) + 1;
    if (page < pages) {
      return pageSize;
    } else {
      return (int) (count % pageSize);
    }
  }

  public long getCount() {
    return source.size();
  }

  public String[] getFieldNames() {
    return source.getFieldNames();
  }

  public RyvrSource getSource() {
    return source;
  }

  public String getEtag(long page) {
    long count = source.longSize();
    long pages = (count / this.pageSize) + 1;
    if (page == pages) {
      return Long.toHexString(count) + "." + Long.toHexString(pageSize);
    } else {
      return Long.toHexString(page) + "." + Long.toHexString(pageSize);
    }
  }

  public boolean isArchivePage(long page) {
    long count = source.longSize();
    long pages = (count / this.pageSize) + 1;
    return page != pages;
  }

}
