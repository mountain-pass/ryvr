package au.com.mountainpass.ryvr.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Ryvr {

  private String title;
  protected long page = -1l;
  protected long pages = -1l;
  protected Map<String, List<Map<String, Object>>> rows = new HashMap<>();
  protected Map<String, Link> links = new HashMap<>();

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

  @JsonProperty("_links")
  public Map<String, Link> getLinks() {
    return links;
  }

  public void prev() {
    refreshPage(getPage() - 1l);
  }

  public Long getPage() {
    if (page < 0l) {
      return getPages();
    }
    return page;
  }

  public void next() {
    refreshPage(getPage() + 1l);
  }

  public void first() {
    refreshPage(1l);
  }

  public void last() {
    refreshPage(getPages());
  }

  @JsonIgnore
  public Long getPages() {
    if (pages < 0l) {
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

}
