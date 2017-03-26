package luxmeter.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import luxmeter.model.RequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Objects;

import static luxmeter.Util.NO_BODY_CONTENT;

/**
 * Decorator for a {@link HttpHandler} providing following features:
 * <p>
 * Handles {@link RequestException}s by sending an appropriate
 * HTTP status messages to the client and logging the exception.
 * Additionally, it ensures that the resources hold by the {@link HttpExchange} object are closed
 * when the processing is finished.
 * </p>
 */
public final class ContextManager implements HttpHandler {
    private final HttpHandler handler;
    private static final Logger logger = LoggerFactory.getLogger(ContextManager.class);

    private ContextManager(@Nonnull HttpHandler handler) {
        this.handler = handler;
    }

    public static ContextManager decorate(@Nonnull HttpHandler handler) {
        Objects.requireNonNull(handler);
        return new ContextManager(handler);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Objects.requireNonNull(exchange);
        try {
            handler.handle(exchange);
        }
        catch (RequestException e) {
            logger.warn(String.format(
                    "Unable to further process the request. Returning with %s.:  ", e.getStatusCode()), e);
            exchange.sendResponseHeaders(e.getStatusCode(), NO_BODY_CONTENT);
        }
        catch (Exception e) {
            logger.warn("Caught unexpected exception: ", e);
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, NO_BODY_CONTENT);
        }
        finally {
            exchange.getRequestBody().close();
            exchange.getResponseBody().close();
            exchange.close();
        }
    }
}
