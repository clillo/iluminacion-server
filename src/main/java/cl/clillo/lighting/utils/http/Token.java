package cl.clillo.lighting.utils.http;

import cl.clillo.lighting.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;

public class Token {

    private final String accessToken;
    private final long expiresIn;

    public Token(final JsonNode jsonNode) {
        String tokenType = JsonUtils.stringValue(jsonNode, "token_type");
        this.accessToken = tokenType.concat(" ").concat(JsonUtils.stringValue(jsonNode, "access_token"));
        long tokenExpires = (JsonUtils.longValue(jsonNode, "expires_in") * 1000) - 10_000;
        this.expiresIn = System.currentTimeMillis() + tokenExpires;
    }

    public boolean isValid() {
        return System.currentTimeMillis() < expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
