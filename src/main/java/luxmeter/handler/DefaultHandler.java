package luxmeter.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import luxmeter.model.Directory;
import luxmeter.model.RequestMethod;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.EnumSet;

import static luxmeter.Util.*;
import static luxmeter.model.HeaderFieldContants.CONTENT_TYPE;
import static luxmeter.model.HeaderFieldContants.ETAG;
import static org.apache.commons.lang3.EnumUtils.getEnum;

/**
 * Simple HttpHandler serving static files:
 * <ul>
 * <li>Can process GET and HEAD requests</li>
 * <li>Lists recursively all files and subdirectories if the request URL points to a directory</li>
 * <li>Can process following header-fields: ETag, If-Non-Match, If-Modified-Since</li>
 * </ul>
 */
public final class DefaultHandler implements HttpHandler {
    private final Path rootDir;

    public DefaultHandler(@Nonnull Path rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        RequestMethod requestMethod = getEnum(RequestMethod.class, exchange.getRequestMethod().toUpperCase());
        if (EnumSet.of(RequestMethod.HEAD, RequestMethod.GET).contains(requestMethod)) {
            Path absolutePath = getAbsoluteSystemPath(rootDir, exchange.getRequestURI());
            File fileOrDirectory = absolutePath.toFile();

            if (fileOrDirectory.isDirectory()) {
                listFiles(exchange, requestMethod, absolutePath);
            }
            // is file
            else {
                sendFile(exchange, requestMethod, fileOrDirectory);
            }
        }
        closeResources(exchange);
    }

    private void closeResources(HttpExchange exchange) throws IOException {
        exchange.getRequestBody().close();
        // otherwise an IO exception is thrown by PlaceholderOutputStream
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.getResponseBody().close();
        }
        exchange.close();
    }

    private void sendFile(@Nonnull HttpExchange exchange,
                          @Nonnull RequestMethod requestMethod,
                          @Nonnull File file) throws IOException {
        long responseLength = file.length();
        String hashCode = generateHashCode(file);
        if (hashCode != null) {
            exchange.getResponseHeaders().add(ETAG, hashCode);
        }
        // the length  is also set for head requests (also expected by the RFC)
        // ignore the warning from ServerImpl
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseLength);
        String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file);
        exchange.getResponseHeaders().add(CONTENT_TYPE, contentType);
        processGetRequest(exchange, requestMethod, file);
    }

    private void processGetRequest(@Nonnull HttpExchange exchange, @Nonnull RequestMethod requestMethod, @Nonnull File file) throws IOException {
        if (requestMethod == RequestMethod.GET) {
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                // Charset charset = Charset.forName(new TikaEncodingDetector().guessEncoding(in));
                for (int data = in.read(); data != -1; data = in.read()) {
                    exchange.getResponseBody().write(data);
                }
            }
        }
    }

    private void listFiles(@Nonnull HttpExchange exchange,
                           @Nonnull RequestMethod requestMethod,
                           @Nonnull Path absolutePath) throws IOException {
        // TODO render to HTML page
        Directory directory = Directory.listFiles(absolutePath);
        String output = directory.toString(rootDir);
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, output.length());
        if (requestMethod == RequestMethod.HEAD) {
            exchange.getResponseHeaders().add(CONTENT_TYPE, "text/plain");
        }
        // is GET
        else {
            exchange.getResponseBody().write(output.getBytes());
        }
    }
}
