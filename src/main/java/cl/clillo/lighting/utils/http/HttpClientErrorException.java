package cl.clillo.lighting.utils.http;

public class HttpClientErrorException extends RuntimeException{

    public HttpClientErrorException(String message, Throwable cause) {
        super(message, cause);
    }

}
