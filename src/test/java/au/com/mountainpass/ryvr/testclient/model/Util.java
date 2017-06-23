package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

public class Util {

  public static void assertEqual(Object actualValue, String expectedValue) throws Throwable {
    if (actualValue instanceof BigDecimal) {
      assertThat((BigDecimal) actualValue, closeTo(new BigDecimal(expectedValue), BigDecimal.ZERO));
    } else if (actualValue instanceof String) {
      assertThat(actualValue, equalTo(expectedValue));
    } else if (actualValue instanceof Long) {
      long expectedNumber = new BigDecimal(expectedValue).longValueExact();
      assertThat(actualValue, equalTo(expectedNumber));
    } else if (actualValue instanceof Boolean) {
      assertThat(actualValue, equalTo(Boolean.parseBoolean(expectedValue)));
    } else {
      Object expectedValueOfType;
      expectedValueOfType = actualValue.getClass().getConstructor(String.class)
          .newInstance(expectedValue);
      assertThat(actualValue, equalTo(expectedValueOfType));
    }
  }

}
