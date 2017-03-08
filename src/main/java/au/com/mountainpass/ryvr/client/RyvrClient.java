package au.com.mountainpass.ryvr.client;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;

public interface RyvrClient {

    public CompletableFuture<ResponseEntity<?>> getApiDocs(
            javax.ws.rs.core.MediaType mediaType);

}
