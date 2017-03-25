package luxmeter;

import javax.annotation.Nonnull;

public class RequestException extends RuntimeException {
    private final int statusCode;
    public RequestException(int statusCode) {
        super("" + statusCode);
        this.statusCode = statusCode;
    }

    public RequestException(int statusCode, @Nonnull Throwable cause) {
        super("" + statusCode, cause);
        this.statusCode = statusCode;

    }

    public int getStatusCode() {
        return statusCode;
    }
}
