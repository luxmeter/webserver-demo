package luxmeter.filter;

import com.sun.net.httpserver.HttpExchange;
import luxmeter.model.RequestException;
import luxmeter.model.RequestMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.EnumSet;

import static luxmeter.Util.NO_BODY_CONTENT;
import static luxmeter.Util.getAbsoluteSystemPath;
import static org.apache.commons.lang3.EnumUtils.getEnum;


public class RequestValidationFilter extends AbstractFilter {
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
        }
        catch (RequestException e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(e.getStatusCode(), NO_BODY_CONTENT);
        }

        // if no exception has been thrown yet:
        if (exchange.getResponseCode() == -1) {
            continueChain(exchange, chain);
        }
    }

    private void checkNonNull(HttpExchange exchange) {
        if (exchange == null) {
            throw  new RequestException(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    private void checkMethodIsSupported(@Nullable RequestMethod requestMethod) {
        if (requestMethod == null) {
            throw new RequestException(HttpURLConnection.HTTP_BAD_METHOD);
        }
    }

    private void checkFileOrDirectoryExists(@Nonnull File fileOrDirectory) {
        if (!fileOrDirectory.exists()) {
            throw new RequestException(HttpURLConnection.HTTP_NOT_FOUND);
        }
    }
}
