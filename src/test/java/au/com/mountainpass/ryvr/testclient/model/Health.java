package au.com.mountainpass.ryvr.testclient.model;

import org.springframework.boot.actuate.health.Status;

public class Health {
    public Status status;

    public Health() {
        status = Status.UNKNOWN;
    }
}