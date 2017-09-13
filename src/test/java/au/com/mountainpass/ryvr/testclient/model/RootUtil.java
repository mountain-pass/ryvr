package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;

import au.com.mountainpass.ryvr.model.RyvrRoot;

public class RootUtil {

    public static void assertHasLink(RyvrRoot root, String title) {
        List<String> linkTitles = root.getLinks().stream().map(link -> {
            return link.getTitle();
        }).collect(Collectors.toList());
        assertThat(linkTitles, hasItem(title));
    }
}
