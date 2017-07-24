package au.com.mountainpass.ryvr.model;

public class Ryvr {

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

  public int getPageSize() {
    return this.pageSize;
  }

  // public long getPages() {
  // return (source.longSize() / this.pageSize) + 1;
  // }

  // public int getCurrentPageSize(long page) {
  // long count = source.longSize();
  // long pages = (count / this.pageSize) + 1;
  // if (page < pages) {
  // return pageSize;
  // } else {
  // return (int) (count % pageSize);
  // }
  // }

  // public long getCount() {
  // return source.size();
  // }

  public String[] getFieldNames() {
    return source.getFieldNames();
  }

  public RyvrSource getSource() {
    return source;
  }

  // public String getEtag(long page) {
  // long count = source.longSize();
  // long pages = (count / this.pageSize) + 1;
  // if (page <= 0 || page == pages) {
  // return Long.toHexString(count) + "." + Long.toHexString(pageSize);
  // } else {
  // return Long.toHexString(page) + "." + Long.toHexString(pageSize);
  // }
  // }

  // public boolean isArchivePage(long page) {
  // if (page <= 0) {
  // return false;
  // }
  // long count = source.longSize();
  // long pages = (count / this.pageSize) + 1;
  // return page != pages;
  // }

}
