package au.com.mountainpass.ryvr.model;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import au.com.mountainpass.ryvr.testclient.model.SwaggerImpl;

public interface RyvrRootImpl {

  SwaggerImpl getApiDocs() throws ClientProtocolException, IOException;

  RyvrsCollection getRyvrsCollection() throws ClientProtocolException, IOException;

  void login(String username, String password) throws ClientProtocolException, IOException;

}
