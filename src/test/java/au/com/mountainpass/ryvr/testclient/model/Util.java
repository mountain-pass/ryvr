package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import cucumber.api.PendingException;

public class Util {

    public static void assertEqual(Object actualValue, String expectedValue) {
        if (actualValue instanceof BigDecimal) {
            assertThat((BigDecimal) actualValue,
                    closeTo(new BigDecimal(expectedValue), BigDecimal.ZERO));
        } else if (actualValue instanceof String) {
            assertThat(actualValue, equalTo(expectedValue));
        } else {
            Object expectedValueOfType;
            try {
                expectedValueOfType = actualValue.getClass()
                        .getConstructor(String.class)
                        .newInstance(expectedValue);
                assertThat(actualValue, equalTo(expectedValueOfType));
            } catch (InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                throw new PendingException();
            }
        }
    }

}
