package au.com.mountainpass.ryvr.datasource;

import java.sql.SQLException;

import au.com.mountainpass.ryvr.model.Field;

class DataSourceField implements Field {
  /**
   * 
   */
  private final DataSourceRyvrSource dataSourceRyvrSource;

  /**
   * @param dataSourceRyvrSource
   */
  DataSourceField(DataSourceRyvrSource dataSourceRyvrSource) {
    this.dataSourceRyvrSource = dataSourceRyvrSource;
  }

  private int fieldIndex;

  @Override
  public Object getValue() {
    // plus one because the first column is column 1, not 0.
    try {
      return this.dataSourceRyvrSource.getRowSet().getObject(fieldIndex + 1);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getName() {
    return this.dataSourceRyvrSource.columnNames[fieldIndex];
  }

  @Override
  public void setFieldIndex(int fieldIndex) {
    this.fieldIndex = fieldIndex;
  }
}