package luxmeter.filter;

import com.sun.net.httpserver.HttpExchange;
import luxmeter.Util;
import luxmeter.model.SupportedRequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.EnumSet;

import static luxmeter.Util.NO_BODY_CONTENT;
import static luxmeter.Util.getAbsoluteSystemPath;
import static luxmeter.model.HeaderFieldContants.CONNECTION;

/**
 * Validates incoming requests, e.g. if the requested file exists and the requested method is supported.
 */
public class RequestValidationFilter extends AbstractFilter {
    static final Logger logger = LoggerFactory.getLogger(RequestValidationFilter.class);
    static final String ERROR_MSG_NOT_SUPPORTED_REQUEST = "The requested method is currently not supported :(";
    static final String ERROR_MSG_RESOURCE_NOT_FOUND = "URL neither points to an existing directory nor file.";

    // makes the validation routine a bit easier to handle
    // each validation can force the request to be aborted with an appropriate status message
    private static final class ValidationException extends RuntimeException {
        private final int statusCode;
        private final String message;
        ValidationException(int statusCode) {
            this(statusCode, null);
        }

        ValidationException(int statusCode, String message) {
            super("" + statusCode);
            this.statusCode = statusCode;
            this.message = message;
        }

        int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "ValidationException{" +
                    "statusCode=" + statusCode +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    public RequestValidationFilter(Path rootDir) {
        super(rootDir);
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        try {
            checkNonNull(exchange);

            SupportedRequestMethod requestMethod = SupportedRequestMethod.of(exchange.getRequestMethod());
            checkMethodIsSupported(requestMethod);

            if (EnumSet.of(SupportedRequestMethod.HEAD, SupportedRequestMethod.GET).contains(requestMethod)) {
                Path absolutePath = getAbsoluteSystemPath(getRootDir(), exchange.getRequestURI());
                File fileOrDirectory = absolutePath.toFile();
                checkFileOrDirectoryExists(fileOrDirectory);
            }
        } catch (ValidationException e) {
            logger.error(String.format("Request (%s) validation failed: %s", exchange.getRequestURI().getPath(), e.getStatusCode()), e);
            if (e.getMessage() == null) {
                exchange.sendResponseHeaders(e.getStatusCode(), NO_BODY_CONTENT);
            }
            else {
                byte[] bytes = (e.getStatusCode() +": " + e.getMessage()).getBytes();
                exchange.sendResponseHeaders(e.getStatusCode(), bytes.length);
                exchange.getResponseBody().write(bytes);
            }
        }

        // if no exception has been thrown yet:
        if (exchange.getResponseCode() == -1) {
            // TODO no one would expect this code in a validator -> move it
            // workaround
            // no need to check other Connection values since the ServerImpl handles that already
            Util.getHeaderFieldValues(exchange.getRequestHeaders(), CONNECTION).stream().findFirst()
                    .filter("close"::equalsIgnoreCase)
                    .ifPresent(value -> {
                        // ServerImpl will close the connection when it finds this in the request
                        // with exactly this strings
                        exchange.getResponseHeaders().add(CONNECTION, "close");
                    });
            continueChain(exchange, chain);
        }
    }

    private void checkNonNull(HttpExchange exchange) {
        if (exchange == null) {
            throw new ValidationException(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    private void checkMethodIsSupported(@Nullable SupportedRequestMethod requestMethod) {
        if (requestMethod == null) {
            throw new ValidationException(HttpURLConnection.HTTP_BAD_METHOD,
                    ERROR_MSG_NOT_SUPPORTED_REQUEST);
        }
    }

    private void checkFileOrDirectoryExists(@Nonnull File fileOrDirectory) {
        if (!fileOrDirectory.exists()) {
            throw new ValidationException(HttpURLConnection.HTTP_NOT_FOUND,
                    ERROR_MSG_RESOURCE_NOT_FOUND);
        }
    }
}
