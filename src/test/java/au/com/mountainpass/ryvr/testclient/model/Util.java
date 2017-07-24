package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

public class Util {

  public static void assertEqual(String msg, Object actualValue, String expectedValue)
      throws Throwable {
    if (actualValue == null) {
      assertThat(msg, expectedValue, nullValue());
    }
    if (expectedValue == null) {
      assertThat(msg, actualValue, nullValue());
    }
    if (actualValue instanceof BigDecimal) {
      assertThat(msg, (BigDecimal) actualValue,
          closeTo(new BigDecimal(expectedValue), BigDecimal.ZERO));
    } else if (actualValue instanceof String) {
      assertThat(msg, actualValue, equalTo(expectedValue));
    } else if (actualValue instanceof Long) {
      long expectedNumber = new BigDecimal(expectedValue).longValueExact();
      assertThat(msg, actualValue, equalTo(expectedNumber));
    } else if (actualValue instanceof Boolean) {
      assertThat(msg, actualValue, equalTo(Boolean.parseBoolean(expectedValue)));
    } else {
      Object expectedValueOfType;
      expectedValueOfType = actualValue.getClass().getConstructor(String.class)
          .newInstance(expectedValue);
      assertThat(msg, actualValue, equalTo(expectedValueOfType));
    }
  }

}
