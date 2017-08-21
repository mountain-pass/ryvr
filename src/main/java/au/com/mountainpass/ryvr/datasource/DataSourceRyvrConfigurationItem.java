package au.com.mountainpass.ryvr.datasource;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotEmpty;

public class DataSourceRyvrConfigurationItem {

  @NotEmpty
  private String query;

  @Min(1)
  private int pageSize;

  public String getQuery() {
    return query;
  }

  /**
   * @return the pageSize
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * @param pageSize
   *          the pageSize to set
   */
  public void setPageSize(final int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * @param query
   *          the query to set
   */
  public void setQuery(String query) {
    this.query = query;
  }

}