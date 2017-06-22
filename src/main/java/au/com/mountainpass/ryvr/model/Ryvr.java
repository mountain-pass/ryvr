package au.com.mountainpass.ryvr.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Ryvr {

  private String title;
  protected long page = -1l;
  protected long pages = -1l;
  protected Map<String, List<Map<String, Object>>> rows = new HashMap<>();
  protected Map<String, Link[]> links = new HashMap<>();

  private Ryvr() {
  }

  public Ryvr(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public boolean refreshPage(long page) {
    throw new NotImplementedException();
  }

  public void refresh() {
    throw new NotImplementedException();
  }

  @JsonProperty("_embedded")
  public Map<String, List<Map<String, Object>>> getEmbedded() {
    return rows;
  }

  @JsonIgnore
  public Map<String, Link[]> getLinks() {
    return links;
  }

  public void prev() {
    refreshPage(getPage() - 1L);
  }

  public long getPage() {
    if (page < 0L) {
      return getPages();
    }
    return page;
  }

  public void next() {
    refreshPage(getPage() + 1L);
  }

  public void first() {
    refreshPage(1L);
  }

  public void last() {
    refreshPage(getPages());
  }

  @JsonIgnore
  public long getPages() {
    if (pages < 0L) {
      refresh();
    }
    return pages;
  }

  public void current() {
    refresh();
  }

  public void self() {
    refreshPage(page);
  }

  @JsonIgnore
  public String getEtag() {
    throw new NotImplementedException();
  }

  public void toJson(OutputStream outputStream) throws IOException {
    throw new NotImplementedException();
  }

  public abstract Iterator<Record> iterator();

  public abstract long getCount();

  public abstract int getPageSize();

  public int getCurrentPageSize() {
    if (page == pages) {
      return (int) (getCount() % getPageSize());
    } else {
      return getPageSize();
    }
  }

  public abstract String[] getFieldNames();
}
