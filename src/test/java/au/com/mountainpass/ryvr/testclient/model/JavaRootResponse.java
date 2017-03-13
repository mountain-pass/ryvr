package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;

import au.com.mountainpass.ryvr.model.Root;

public class JavaRootResponse implements RootResponse {

    private Root root;

    public JavaRootResponse(Root root) {
        this.root = root;
    }

    @Override
    public void assertHasApiDocsLink() {
        assertHasLink("API Docs");
    }

    private void assertHasLink(String title) {
        List<String> linkTitles = root.getLinks().stream().map(link -> {
            return link.getTitle();
        }).collect(Collectors.toList());
        assertThat(linkTitles, hasItem(title));
    }

    @Override
    public void assertHasRyvrsLink() {
        assertHasLink("Ryvrs");
    }

}
