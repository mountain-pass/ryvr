package au.com.mountainpass.ryvr.datasource;

import au.com.mountainpass.ryvr.model.Field;
import au.com.mountainpass.ryvr.model.Record;

class DataSourceRecord implements Record {
  /**
   * 
   */
  final Field field;

  private final DataSourceRyvrSource dataSourceRyvrSource;

  /**
   * @param dataSourceRyvrSource
   */
  DataSourceRecord(DataSourceRyvrSource dataSourceRyvrSource) {
    this.dataSourceRyvrSource = dataSourceRyvrSource;
    field = new DataSourceField(dataSourceRyvrSource);
  }

  @Override
  public int size() {
    return this.dataSourceRyvrSource.getFieldNames().length;
  }

  @Override
  public Field getField(int fieldIndex) {
    field.setFieldIndex(fieldIndex);
    return field;
  }
}