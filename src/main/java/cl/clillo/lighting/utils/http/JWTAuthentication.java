package cl.clillo.lighting.utils.http;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.util.HashMap;
import java.util.Map;

public class JWTAuthentication implements IHttpAuthentication {

    private final String tokenEndpointURL;
    private final Map<String, String> tokenEndpointHeaders;
    private final Map<String, String> tokenBodyParams;

    public JWTAuthentication(final String client, final String secret,
                             final String tokenEndpointURL, final Map<String, String> tokenBodyParams) {
        this.tokenEndpointURL = tokenEndpointURL;
        this.tokenEndpointHeaders = new HashMap<>();
        this.tokenBodyParams = new HashMap<>(tokenBodyParams);
        this.tokenBodyParams.put("client_id", client);
        this.tokenBodyParams.put("client_secret", secret);
        tokenEndpointHeaders.put("Content-Type", "application/json");
    }


    @Override
    public Token getToken(HttpClient httpClient, HttpClientResponseHandler<JsonNode> response) {
        final HttpPost httpPost = new HttpPost(tokenEndpointURL);
/*
        if (ObjectUtils.isNotEmpty(tokenBodyParams))
            httpPost.setEntity(new StringEntity(JsonUtils.convertTo(tokenBodyParams)));

        for (Map.Entry<String, String> entry : tokenEndpointHeaders.entrySet())
            httpPost.setHeader(entry.getKey(), entry.getValue());

        try {
            return new Token(httpClient.execute(httpPost, response));
        } catch (HttpClientException exception) {
            throw exception;
        } catch (Exception ex) {
            throw new HttpClientErrorException(ex.getMessage(), ex);
        }*/

        return null;
    }
}
