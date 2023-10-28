package cl.clillo.lighting.utils.http;

import cl.clillo.lighting.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.databind.util.ClassUtil.nonNull;

public class HttpClient {
    private static final String USE_SOCS_PROXY = "useSocsProxy";
    private static final String TIMEOUT = "timeout";
    private final CloseableHttpClient closeableHttpClient;
    private final HttpClientContext context;
    private final ObjectMapper jsonMapper;
    private final HttpClientResponseHandler<JsonNode> responseJsonHandler;
    private final HttpClientResponseHandler<String> responseXMLHandler;

    private final HttpClientResponseHandler<byte[]> responseByteArrayHandler;

    private boolean isOauthActive;
    private Token token;
    private IHttpAuthentication httpAuthentication;

    public HttpClient() {
        this(Map.of());
    }

    public HttpClient(final Map<String, Object> configurationParams) {
        this.closeableHttpClient = buildHttpClient(configurationParams);
        this.context = buildHttpContext(configurationParams);
        this.jsonMapper = buildObjectMapper();
        this.responseJsonHandler = buildResponseJsonHandler();
        this.responseXMLHandler = buildResponseXMLHandler();
        this.responseByteArrayHandler = buildResponseByteArrayHandler();
        this.isOauthActive = false;
    }

    public HttpClient(final IHttpAuthentication httpAuthentication) {
        this(Map.of());
        this.isOauthActive = true;
        this.httpAuthentication = httpAuthentication;
    }

    public JsonNode get(final String uri, final Map<String, String> headers, final Map<String, String> params) {
        try {
            final URIBuilder uriObj = new URIBuilder(uri);
            for (Map.Entry<String, String> entry : params.entrySet())
                uriObj.addParameter(new BasicNameValuePair(entry.getKey(), entry.getValue()));

            return get(uriObj.build(), headers);
        } catch (URISyntaxException use) {
            return null;
        }
    }

    public String getString(final String uri, final Map<String, String> headers, final Map<String, String> params) {
        try {
            final URIBuilder uriObj = new URIBuilder(uri);
            for (Map.Entry<String, String> entry : params.entrySet())
                uriObj.addParameter(new BasicNameValuePair(entry.getKey(), entry.getValue()));

            return getString(uriObj.build(), headers);
        } catch (URISyntaxException use) {
            return null;
        }
    }

    public JsonNode get(final URI uri, final Map<String, String> headers) {
        return jsonSend(new HttpGet(uri), headers);
    }

    public String getString(final URI uri, final Map<String, String> headers) {
        return stringSend(new HttpGet(uri), headers);
    }

    public<T> T get(final String uri, final Map<String, String> headers, final Map<String, String> params, TypeReference<T> typeReference) {
        try {
            final URIBuilder uriObj = new URIBuilder(uri);
            for (Map.Entry<String, String> entry : params.entrySet())
                uriObj.addParameter(new BasicNameValuePair(entry.getKey(), entry.getValue()));

            if(typeReference.getType().equals(byte[].class)) {
                HttpGet httpGet = new HttpGet(uriObj.build());
                for (Map.Entry<String, String> entry : headers.entrySet())
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                return (T)closeableHttpClient.execute(httpGet, context, responseByteArrayHandler);
            }
            JsonNode jsonNode = jsonSend(new HttpGet(uriObj.build()), headers);
            return JsonUtils.convertTo(jsonNode,typeReference);
        } catch (URISyntaxException | IOException e) {
            throw new HttpClientErrorException("Error executing GET request: " + e.getMessage(), e);
        }
    }

    public <T> JsonNode post(final String uri, final Map<String, String> headers, final T body) {
        return jsonSend(new HttpPost(uri), headers, body);
    }

    public <T> JsonNode post(final URI uri, final Map<String, String> headers, final T body) {
        return jsonSend(new HttpPost(uri), headers, body);
    }

    public JsonNode postMultiPart(final String uri, final Map<String, String> headers, final List<NameValuePair> formParams) {
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(new UrlEncodedFormEntity(formParams));
        return jsonSend(httpPost, headers, null);
    }

    public JsonNode postFormData(final String uri, final Map<String, String> headers, final Map<String, String> params) {
        final StringBuilder formBody = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (formBody.length() > 0) {
                formBody.append("&");
            }
            formBody.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            formBody.append("=");
            formBody.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        final ClassicHttpRequest httpMethod = new HttpPost(uri);

        for (Map.Entry<String, String> entry : headers.entrySet())
            httpMethod.addHeader(entry.getKey(), entry.getValue());

        httpMethod.setEntity(new StringEntity(formBody.toString(), StandardCharsets.UTF_8));
        httpMethod.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED);

        try {
            return closeableHttpClient.execute(httpMethod, context, responseJsonHandler);

        } catch (IOException ex) {
            throw new RuntimeException("Error creating connection with URL: " + uri, ex);
        }
    }

    public <T> JsonNode put(final URI uri, final Map<String, String> headers, final T body) {
        return jsonSend(new HttpPut(uri), headers, body);

    }

    public <T> JsonNode patch(final String uri, final Map<String, String> headers, final T body) {
        return jsonSend(new HttpPatch(uri), headers, body);

    }

    public <T> JsonNode patch(final URI uri, final Map<String, String> headers, final T body) {
        return jsonSend(new HttpPatch(uri), headers, body);
    }

    public <T> JsonNode delete(final URI uri, final Map<String, String> headers, final T body) {
        return jsonSend(new HttpDelete(uri), headers, body);
    }

    private JsonNode jsonSend(final ClassicHttpRequest httpMethod, final Map<String, String> headers) {
        return jsonSend(httpMethod, headers, null);
    }

    private String stringSend(final ClassicHttpRequest httpMethod, final Map<String, String> headers) {
        return stringSend(httpMethod, headers, null);
    }

    private <T> JsonNode jsonSend(final ClassicHttpRequest httpMethod, final Map<String, String> headers, final T body) {
        try {
            if (isOauthActive)
                includeOauthHeaders(httpMethod);

            if (body != null)
                httpMethod.setEntity(body instanceof HttpEntity? (HttpEntity)body : new StringEntity(objectToJsonString(body), StandardCharsets.UTF_8));

            if(!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                httpMethod.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON);
            }

            for (Map.Entry<String, String> entry : headers.entrySet())
                httpMethod.addHeader(entry.getKey(), entry.getValue());

            return closeableHttpClient.execute(httpMethod, context, responseJsonHandler);
        } catch (HttpClientException exception) {
            throw exception;
        } catch (Exception ex) {
            throw new HttpClientErrorException(ex.getMessage(), ex);
        }

    }

    private <T> String stringSend(final ClassicHttpRequest httpMethod, final Map<String, String> headers, final T body) {
        try {
            if (isOauthActive)
                includeOauthHeaders(httpMethod);

            if (body != null)
                httpMethod.setEntity(body instanceof HttpEntity? (HttpEntity)body : new StringEntity(objectToJsonString(body), StandardCharsets.UTF_8));

            if(!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                httpMethod.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON);
            }

            for (Map.Entry<String, String> entry : headers.entrySet())
                httpMethod.addHeader(entry.getKey(), entry.getValue());

            return closeableHttpClient.execute(httpMethod, context, responseXMLHandler);
        } catch (HttpClientException exception) {
            throw exception;
        } catch (Exception ex) {
            throw new HttpClientErrorException(ex.getMessage(), ex);
        }

    }

    public String soapSend(final String uri, final String soapAction, final String bodyXML) {
        final ClassicHttpRequest httpMethod = new HttpPost(uri);

        httpMethod.setEntity(new StringEntity(bodyXML, StandardCharsets.UTF_8));
        httpMethod.setHeader("SOAPAction", soapAction);
        httpMethod.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_XML);

        try {
            return closeableHttpClient.execute(httpMethod, context, responseXMLHandler);

        } catch (IOException ex) {
            throw new RuntimeException("Error creating connection with URL: " + uri, ex);
        }
    }

    public String objectToJsonString(final Object message) throws JsonProcessingException {
        String objectJson = this.jsonMapper.writeValueAsString(message).replace("\\\\", "");

        if (objectJson.startsWith("\"")) {
            objectJson = objectJson.replaceFirst("\"", "").trim();
        }
        if (objectJson.charAt(objectJson.length() - 1) == '"') {
            objectJson = objectJson.substring(0, objectJson.length() - 1);
        }
        return objectJson;
    }

    private CloseableHttpClient buildHttpClient(final Map<String, Object> configurationParams) {
        final boolean useSocsProxy = configurationParams.containsKey(USE_SOCS_PROXY) && (Boolean) configurationParams.get(USE_SOCS_PROXY);

        final PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultSocketConfig(SocketConfig.custom()
                        .setSoTimeout(Timeout.ofMinutes(1))
                        .build())
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
                .setConnPoolPolicy(PoolReusePolicy.LIFO)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setSocketTimeout(Timeout.ofMilliseconds(Long.parseLong((String)nonNull(configurationParams.get(TIMEOUT), "300000"))))
                        .setConnectTimeout(Timeout.ofMilliseconds(Long.parseLong((String)nonNull(configurationParams.get(TIMEOUT), "300000"))))
                        .setTimeToLive(TimeValue.ofMinutes(10))
                        .build());

        if (useSocsProxy)
            connectionManagerBuilder.setSSLSocketFactory(new SOCSConnectionSocketFactory(SSLContexts.createSystemDefault()));

        final PoolingHttpClientConnectionManager connectionManager = connectionManagerBuilder.build();

        connectionManager.setMaxTotal(500);
        connectionManager.setDefaultMaxPerRoute(500);

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(StandardCookieSpec.STRICT)
                        .build())
                .build();
    }

    private HttpClientContext buildHttpContext(final Map<String, Object> configurationParams) {
        final boolean useSocsProxy = configurationParams.containsKey(USE_SOCS_PROXY) && (Boolean) configurationParams.get(USE_SOCS_PROXY);

        final CookieStore cookieStore = new BasicCookieStore();

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        HttpClientContext httpClientContext = HttpClientContext.create();
        httpClientContext.setCookieStore(cookieStore);
        httpClientContext.setCredentialsProvider(credentialsProvider);
        httpClientContext.setRequestConfig(RequestConfig.custom()
                .setCookieSpec(StandardCookieSpec.STRICT)
                .build());

        if (useSocsProxy) {
            final String proxySocsHost = (String) configurationParams.get("proxySocsHost");
            final int proxySocsPort = (Integer) configurationParams.get("proxySocsPort");

            final InetSocketAddress sockAddress = new InetSocketAddress(proxySocsHost, proxySocsPort);
            httpClientContext.setAttribute("socks.address", sockAddress);
        }

        return httpClientContext;
    }

    private ObjectMapper buildObjectMapper() {
        final JsonFactory jsonFactory = new JsonFactory();
        return new ObjectMapper(jsonFactory);
    }

    private HttpClientResponseHandler<JsonNode> buildResponseJsonHandler() {
        return response -> {
            final int status = response.getCode();
            final HttpEntity entity = response.getEntity();

            JsonNode node = null;
            if(entity != null){
                node = jsonMapper.readTree(EntityUtils.toString(entity));
            }
            if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                return node;
            }

            throw new HttpClientException("Http Client Exception: Http code: " + status, status, node);
        };
    }

    private HttpClientResponseHandler<String> buildResponseXMLHandler() {
        return response -> {
            final int status = response.getCode();
            final HttpEntity entity = response.getEntity();

            if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                return EntityUtils.toString(entity);
            }

            throw new RuntimeException("Failed request: Http code: " + status);
        };
    }

    private HttpClientResponseHandler<byte[]> buildResponseByteArrayHandler() {
        return response -> {
            final int status = response.getCode();
            final HttpEntity entity = response.getEntity();

            if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                return EntityUtils.toByteArray(entity);
            }

            throw new RuntimeException("Failed request: Http code: " + status);
        };
    }

    private static class SOCSConnectionSocketFactory extends SSLConnectionSocketFactory {
        public SOCSConnectionSocketFactory(final SSLContext sslContext) {
            super(sslContext);
        }

        @Override
        public Socket createSocket(final HttpContext context) {
            final InetSocketAddress sockAddress = (InetSocketAddress) context.getAttribute("socks.address");
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, sockAddress);
            return new Socket(proxy);
        }
    }

    private void includeOauthHeaders(final ClassicHttpRequest httpMethod) {
        if (token == null) {
            try {
             //   this.token = httpAuthentication.getToken(closeableHttpClient, responseJsonHandler);
            } catch (Exception e) {
                throw new HttpClientErrorException(e.getMessage(), e);
            }
        }
        //if (!token.isValid())
            //token = httpAuthentication.getToken(closeableHttpClient, responseJsonHandler);

        Objects.requireNonNull(token);
        httpMethod.addHeader("Authorization", token.getAccessToken());
    }
}
