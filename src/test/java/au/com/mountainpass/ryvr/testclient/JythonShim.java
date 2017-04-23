package au.com.mountainpass.ryvr.testclient;

import org.springframework.stereotype.Component;

@Component
public class JythonShim {

    private static RestRyvrClient restRyvrClient;

    /**
     * @return the restRyvrClient
     */
    public static RestRyvrClient getRestRyvrClient() {
        return restRyvrClient;
    }

    /**
     * @param restRyvrClient
     *            the restRyvrClient to set
     */
    public static void setRestRyvrClient(RestRyvrClient restRyvrClient) {
        JythonShim.restRyvrClient = restRyvrClient;
    }

}
