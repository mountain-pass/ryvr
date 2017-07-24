package au.com.mountainpass.ryvr.datasource;

import au.com.mountainpass.ryvr.model.Field;
import au.com.mountainpass.ryvr.model.Record;

class DataSourceRecord implements Record {
  /**
   * 
   */
  private final DataSourceRyvrSource dataSourceRyvrSource;

  /**
   * @param dataSourceRyvrSource
   */
  DataSourceRecord(DataSourceRyvrSource dataSourceRyvrSource) {
    this.dataSourceRyvrSource = dataSourceRyvrSource;
  }

  @Override
  public int size() {
    return this.dataSourceRyvrSource.getFieldNames().length;
  }

  @Override
  public Field getField(int fieldIndex) {
    this.dataSourceRyvrSource.field.setFieldIndex(fieldIndex);
    return this.dataSourceRyvrSource.field;
  }
}