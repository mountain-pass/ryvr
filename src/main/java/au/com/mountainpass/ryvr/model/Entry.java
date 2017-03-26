package au.com.mountainpass.ryvr.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class Entry extends MutableHalRepresentation {

    @JsonUnwrapped
    private Map<String, Object> properties = new HashMap<>();

    private Entry() {
    }

    public Entry(Map<String, Object> row) {
        properties = row;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

}
