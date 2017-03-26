package luxmeter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import java.util.EnumSet;

import static org.apache.commons.lang3.EnumUtils.getEnum;

/**
 * Simple HttpHandler serving static files:
 * <ul>
 *     <li>Can process GET and HEAD requests</li>
 *     <li>Lists recursively all files and subdirectories if the request URL points to a directory</li>
 *     <li>Can process following header-fields: ETag, If-Non-Match, If-Modified-Since</li>
 * </ul>
 */
final class DefaultHandler implements HttpHandler {

    public static final int NO_BODY_CONTENT = -1;

    // allows you to see on first sight which requests are supported
    // additionally you don't need to bother with null checks (s. below)
    private enum RequestMethod {
        GET,
        HEAD
    }

    private final Path rootDir;

    public DefaultHandler(@Nonnull Path rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        RequestMethod requestMethod = getEnum(RequestMethod.class, exchange.getRequestMethod().toUpperCase());

        validateRequestMethod(requestMethod);

        if (EnumSet.of(RequestMethod.HEAD, RequestMethod.GET).contains(requestMethod)) {
            Path absolutePath = getAbsoluteSystemPath(exchange.getRequestURI());
            File fileOrDirectory = absolutePath.toFile();

            validateFile(fileOrDirectory);

            if (fileOrDirectory.isDirectory()) {
                listFiles(exchange, requestMethod, absolutePath);
            }
            // is file
            else {
                sendFileContent(exchange, requestMethod, fileOrDirectory);
            }
        }
    }

    private void validateRequestMethod(@Nullable RequestMethod requestMethod) {
        if (requestMethod == null) {
            throw new RequestException(HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    private void validateFile(@Nonnull File fileOrDirectory) {
        if (!fileOrDirectory.exists()) {
            throw new RequestException(HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    private void sendFileContent(@Nonnull HttpExchange exchange,
                                 @Nonnull RequestMethod requestMethod,
                                 @Nonnull File fileOrDirectory) throws IOException {
        long responseLength = fileOrDirectory.length();
        String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(fileOrDirectory);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        if (requestMethod == RequestMethod.HEAD) {
            exchange.getResponseHeaders().add("Content-Length", ""+responseLength);
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, NO_BODY_CONTENT);
        }
        // is GET
        else {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseLength);
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileOrDirectory))) {
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
            exchange.getResponseHeaders().add("Content-Length", ""+responseLength);
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, NO_BODY_CONTENT);
        }
        // is GET
        else {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseLength);
            exchange.getResponseBody().write(output.getBytes());
        }
    }

    private @Nonnull Path getAbsoluteSystemPath(@Nonnull URI uri) {
        // the url could also look like http://localhost:8080 instead of http://localhost:8080/
        String relativePath = uri.getPath();
        if (uri.getPath().length() > 0) {
            relativePath = uri.getPath().substring(1); // get rid of the leading '/'
        }
        return rootDir.resolve(relativePath);
    }
}
