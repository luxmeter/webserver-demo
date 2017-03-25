package luxmeter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Objects;

public final class ContextManager implements HttpHandler {
    private final HttpHandler handler;

    private ContextManager(@Nonnull HttpHandler handler) {
        this.handler = handler;
    }

    public static ContextManager decorate(@Nonnull HttpHandler handler) {
        Objects.requireNonNull(handler);
        return new ContextManager(handler);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            handler.handle(exchange);
        }
        catch (RequestException e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(e.getStatusCode(), -1);
            throw e;
        }
        catch (Exception e) {
            // otherwise it is swallowed
            e.printStackTrace();
        }
        finally {
            // closes also input and output streams
            exchange.close();
        }
    }
}
