package cl.clillo.lighting.utils.http;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

public interface IHttpAuthentication {

    Token getToken(HttpClient httpClient, HttpClientResponseHandler<JsonNode> response);
}
