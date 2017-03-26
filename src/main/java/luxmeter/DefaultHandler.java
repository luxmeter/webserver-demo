package luxmeter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.EnumSet;

import static luxmeter.HeaderFieldContants.ETAG;
import static luxmeter.Util.*;
import static org.apache.commons.lang3.EnumUtils.getEnum;

/**
 * Simple HttpHandler serving static files:
 * <ul>
 * <li>Can process GET and HEAD requests</li>
 * <li>Lists recursively all files and subdirectories if the request URL points to a directory</li>
 * <li>Can process following header-fields: ETag, If-Non-Match, If-Modified-Since</li>
 * </ul>
 */
final class DefaultHandler implements HttpHandler {
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
    }

    private void sendFile(@Nonnull HttpExchange exchange,
                          @Nonnull RequestMethod requestMethod,
                          @Nonnull File file) throws IOException {
        long responseLength = file.length();
        String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        String hashCode = generateHashCode(file);
        if (hashCode != null) {
            exchange.getResponseHeaders().add(ETAG, hashCode);
        }

        if (requestMethod == RequestMethod.HEAD) {
            exchange.getResponseHeaders().add("Content-Length", "" + responseLength);
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, NO_BODY_CONTENT);
        }
        // is GET
        else {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseLength);
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
        long responseLength = output.length();
        if (requestMethod == RequestMethod.HEAD) {
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            exchange.getResponseHeaders().add("Content-Length", "" + responseLength);
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, NO_BODY_CONTENT);
        }
        // is GET
        else {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseLength);
            exchange.getResponseBody().write(output.getBytes());
        }
    }
}
