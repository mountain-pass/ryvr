package au.com.mountainpass.ryvr.datasource;

import javax.validation.constraints.Min;

import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.validator.constraints.NotEmpty;

public class DataSourceRyvrConfigurationItem {

  @NotEmpty
  private String catalog;
  @NotEmpty
  private String orderedBy;
  @Min(1)
  private int pageSize;

  @NotEmpty
  private String table;

  public String getCatalog() {
    return catalog;
  }

  /**
   * @return the orderedBy
   */
  public String getOrderedBy() {
    return orderedBy;
  }

  /**
   * @return the pageSize
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * @return the table
   */
  public String getTable() {
    return table;
  }

  /**
   * @param catalog
   *          the catalog to set
   */
  public void setCatalog(final String catalog) {
    this.catalog = catalog;
  }

  /**
   * @param orderedBy
   *          the orderedBy to set
   */
  public void setOrderedBy(final String orderedBy) {
    this.orderedBy = orderedBy;
  }

  /**
   * @param pageSize
   *          the pageSize to set
   */
  public void setPageSize(final int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * @param table
   *          the table to set
   */
  public void setTable(final String table) {
    this.table = table;
  }

  public String getQuery() {
    throw new NotImplementedException("TODO");
  }

}