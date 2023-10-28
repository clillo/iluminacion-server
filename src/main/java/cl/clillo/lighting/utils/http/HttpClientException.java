package cl.clillo.lighting.utils.http;

import com.fasterxml.jackson.databind.JsonNode;

public class HttpClientException extends RuntimeException{

    private final int statusCode;
    private final transient JsonNode node;

    public HttpClientException(String message, int statusCode, JsonNode node) {
        super(message);
        this.statusCode = statusCode;
        this.node = node;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public JsonNode getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "HttpClientException{" +
                "statusCode=" + statusCode +
                ", node=" + node +
                ", message=" + getMessage() +
                '}';
    }
}
