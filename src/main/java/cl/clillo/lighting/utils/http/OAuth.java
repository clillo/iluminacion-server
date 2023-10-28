package cl.clillo.lighting.utils.http;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OAuth implements IHttpAuthentication{

    private final String tokenEndpointURL;
    private final Map<String, String> tokenEndpointHeaders;
    private final Map<String, String> tokenEndpointBodyParams;

    public OAuth(final String client, final String secret,
                 final String tokenEndpointURL, final Map<String, String> tokenEndpointBodyParams) {
        this.tokenEndpointURL = tokenEndpointURL;
        this.tokenEndpointHeaders = new HashMap<>();
        this.tokenEndpointBodyParams = tokenEndpointBodyParams;
        tokenEndpointHeaders.put("Authorization", "Basic " + Base64.getEncoder().encodeToString( (client+":"+secret).getBytes()));
        tokenEndpointHeaders.put("Content-Type","application/x-www-form-urlencoded");
    }

    @Override
    public Token getToken(HttpClient httpClient, HttpClientResponseHandler<JsonNode> response) {
        final HttpPost httpPost = new HttpPost(tokenEndpointURL);
        final List<NameValuePair> params = new ArrayList<>();

        for (Map.Entry<String, String> entry : tokenEndpointBodyParams.entrySet())
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));

        httpPost.setEntity(new UrlEncodedFormEntity(params));

        for (Map.Entry<String, String> entry : tokenEndpointHeaders.entrySet())
            httpPost.setHeader(entry.getKey(), entry.getValue());

       /* try {
            return new Token(httpClient.execute(httpPost, response));
        } catch (HttpClientException exception) {
            throw exception;
        } catch (Exception ex) {
            throw new HttpClientErrorException(ex.getMessage(), ex);
        }*/

        return null;
    }
}
