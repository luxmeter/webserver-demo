package luxmeter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.lang3.EnumUtils;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;

import static luxmeter.Directory.listFiles;

public class Dispatcher implements HttpHandler {
    // allows you to see on first sight which requests are supported
    // additionally you don't need to bother with null checks (s. below)
    private enum RequestMethod {
        GET,
        HEAD
    }

    private final Path rootDir;

    public Dispatcher(@Nonnull Path rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Objects.requireNonNull(exchange);

        RequestMethod requestMethod = EnumUtils.getEnum(RequestMethod.class, exchange.getRequestMethod().toUpperCase());
        if (EnumSet.of(RequestMethod.HEAD, RequestMethod.GET).contains(requestMethod)) {
            long responseLength = -1;
            Path absolutePath = getAbsoluteSystemPath(exchange);
            File fileOrDirectory = absolutePath.toFile();
            if (fileOrDirectory.exists()) {
                if (fileOrDirectory.isDirectory()) {
                    Directory directory = listFiles(absolutePath);
                    String output = directory.toString(rootDir);
                    if (requestMethod == RequestMethod.GET) {
                        responseLength = output.length();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseLength);
                        exchange.getResponseBody().write(output.getBytes());
                    }
                    else {
                        exchange.getResponseHeaders().add("Content-Type", "text/plain");
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseLength);
                    }
                }
                // is file
                else {
                    if (requestMethod == RequestMethod.GET) {
                        responseLength = fileOrDirectory.length();

                        String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(fileOrDirectory);
                        exchange.getResponseHeaders().add("Content-Type", contentType);

                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseLength);
                        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileOrDirectory))) {
                            for (int data = in.read(); data != -1; data = in.read()) {
                                exchange.getResponseBody().write(data);
                            }
                        }
                    }
                }
            }
            else {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
            }
        }
        else {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1);
        }

        // flush and close
        exchange.getResponseBody().close();
        exchange.getRequestBody().close();
    }

    private Path getAbsoluteSystemPath(HttpExchange exchange) {
        URI uri = exchange.getRequestURI();
        String relativePath = uri.getPath().substring(1);
        return rootDir.resolve(relativePath);
    }
}
