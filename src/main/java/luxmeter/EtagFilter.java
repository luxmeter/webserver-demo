package luxmeter;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

import static luxmeter.Util.NO_BODY_CONTENT;
import static luxmeter.Util.generateHashCode;
import static luxmeter.Util.getAbsoluteSystemPath;

class EtagFilter extends Filter {

    private final Path rootDir;

    public EtagFilter(Path rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        Path absolutePath = getAbsoluteSystemPath(rootDir, exchange.getRequestURI());
        File fileOrDirectory = absolutePath.toFile();
        String hashCode = generateHashCode(fileOrDirectory);
        if (hashCode != null) {
            exchange.getResponseHeaders().add("ETag", hashCode);
        }
        boolean hashCodeIsUnchanged = exchange.getRequestHeaders()
                .getOrDefault("If-none-match", Collections.emptyList()).stream()
                .findFirst()
                .map(requestedHashCode -> hashCode != null && Objects.equals(requestedHashCode, hashCode))
                .orElse(false);
        if (hashCodeIsUnchanged) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, NO_BODY_CONTENT);
        }
        else if (chain != null) {
            chain.doFilter(exchange);
        }
    }

    @Override
    public String description() {
        return getClass().getName();
    }
}