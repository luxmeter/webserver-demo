package luxmeter;

import com.sun.net.httpserver.HttpHandler;

import javax.annotation.Nonnull;
import java.net.HttpURLConnection;

/**
 * Enables you, combined with the {@link ContextManager},
 * to handle validation violations in any {@link HttpHandler} in a more elegant and safer way.
 * @see ContextManager
 */
final class RequestException extends RuntimeException {
    private final int statusCode;

    /**
     * @param statusCode HTTP status code being also used as exception message
     * @see HttpURLConnection HTTP status code constants
     * @see RuntimeException#RuntimeException(String)
     */
    public RequestException(int statusCode) {
        super("" + statusCode);
        this.statusCode = statusCode;
    }

    /**
     * @param statusCode HTTP status code being also used as exception message
     * @param cause the causing exception
     * @see HttpURLConnection HTTP status code constants
     * @see RuntimeException#RuntimeException(Throwable)
     */
    public RequestException(int statusCode, @Nonnull Throwable cause) {
        super("" + statusCode, cause);
        this.statusCode = statusCode;

    }

    public int getStatusCode() {
        return statusCode;
    }
}
