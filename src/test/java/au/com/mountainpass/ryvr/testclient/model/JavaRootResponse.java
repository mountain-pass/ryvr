package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collections;

import javax.ws.rs.core.MediaType;

import au.com.mountainpass.inflector.springboot.controllers.AcceptRouter;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import io.swagger.inflector.models.RequestContext;

public class JavaRootResponse implements RootResponse {

    private Root root;
    private AcceptRouter router;

    public JavaRootResponse(Root root, AcceptRouter router) {
        this.root = root;
        this.router = router;
    }

    @Override
    public void assertHasApiDocsLink() {
        RootUtil.assertHasLink(root, "API Docs");
    }

    @Override
    public void assertHasRyvrsLink() {
        RootUtil.assertHasLink(root, "Ryvrs");
    }

    @Override
    public void assertHasTitle(String title) {
        assertThat(root.getTitle(), equalTo(title));
    }

    @Override
    public RyvrsCollectionResponse followRyvrsLink() {
        RequestContext request = new RequestContext();
        request.setAcceptableMediaTypes(
                Collections.singletonList(MediaType.APPLICATION_JSON_TYPE));
        return new JavaRyvrsCollectionResponse((RyvrsCollection) router
                .getRvyrsCollection(request, null, null, null, null).getBody());
    }

}
