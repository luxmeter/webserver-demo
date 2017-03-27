package luxmeter.filter;

import com.sun.net.httpserver.HttpExchange;
import luxmeter.Util;
import luxmeter.model.RequestMethod;
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
import static org.apache.commons.lang3.EnumUtils.getEnum;


public class RequestValidationFilter extends AbstractFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestValidationFilter.class);
    public static final String ERROR_MSG_NOT_SUPPORTED_REQUEST = "The requested method is currently not supported :(";
    public static final String ERROR_MSG_RESOURCE_NOT_FOUND = "URL neither points to an existing directory nor file.";

    // makes the validation routine a bit easier to handle
    // each validation can force the request to be aborted with an appropriate status message
    private static final class ValidationException extends RuntimeException {
        private final int statusCode;
        private final String message;
        public ValidationException(int statusCode) {
            this(statusCode, null);
        }

        public ValidationException(int statusCode, String message) {
            super("" + statusCode);
            this.statusCode = statusCode;
            this.message = message;
        }

        public int getStatusCode() {
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

            RequestMethod requestMethod = getEnum(RequestMethod.class, exchange.getRequestMethod().toUpperCase());
            checkMethodIsSupported(requestMethod);

            if (EnumSet.of(RequestMethod.HEAD, RequestMethod.GET).contains(requestMethod)) {
                Path absolutePath = getAbsoluteSystemPath(rootDir, exchange.getRequestURI());
                File fileOrDirectory = absolutePath.toFile();
                checkFileOrDirectoryExists(fileOrDirectory);
            }
        } catch (ValidationException e) {
            logger.error(String.format("Request validation failed: ", e.getStatusCode()), e);
            if (e.getMessage() == null) {
                exchange.sendResponseHeaders(e.getStatusCode(), NO_BODY_CONTENT);
            }
            else {
                byte[] bytes = e.getMessage().getBytes();
                exchange.sendResponseHeaders(e.getStatusCode(), bytes.length);
                exchange.getResponseBody().write(bytes);
            }
        }

        // if no exception has been thrown yet:
        if (exchange.getResponseCode() == -1) {
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

    private void checkMethodIsSupported(@Nullable RequestMethod requestMethod) {
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
