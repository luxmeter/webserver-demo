package luxmeter.handler;

import static java.net.HttpURLConnection.HTTP_OK;
import static luxmeter.Util.generateHashCode;
import static luxmeter.Util.getAbsoluteSystemPath;
import static luxmeter.Util.getLastModifiedDate;
import static luxmeter.Util.renderToHtml;
import static luxmeter.model.HeaderFieldContants.CONTENT_TYPE;
import static luxmeter.model.HeaderFieldContants.ETAG;
import static luxmeter.model.HeaderFieldContants.LAST_MODIFIED;
import static luxmeter.model.SupportedRequestMethod.GET;
import static luxmeter.model.SupportedRequestMethod.HEAD;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Optional;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Nonnull;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import luxmeter.model.Directory;
import luxmeter.model.SupportedRequestMethod;

/**
 * Simple HttpHandler serving static files:
 * <ul>
 * <li>Can process GET and HEAD requests</li>
 * <li>Lists recursively all files and subdirectories if the request URL points to a directory</li>
 * </ul>
 */
public final class DefaultHandler implements HttpHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHandler.class);
    private final Path rootDir;

    public DefaultHandler(@Nonnull Path rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Optional<SupportedRequestMethod> supportedMethod = SupportedRequestMethod.of(exchange.getRequestMethod());
        if (supportedMethod.map(r -> EnumSet.of(HEAD, GET).contains(r)).orElse(false)) {
            Path absolutePath = getAbsoluteSystemPath(rootDir, exchange.getRequestURI());

            if (absolutePath.toFile().isDirectory()) {
                listFiles(exchange, supportedMethod.get(), absolutePath);
            }
            // is file
            else {
                sendFile(exchange, supportedMethod.get(), absolutePath);
            }
        }
        closeResources(exchange);
    }

    private void closeResources(@Nonnull HttpExchange exchange) throws IOException {
        exchange.getRequestBody().close();
        // otherwise an IO exception is thrown by PlaceholderOutputStream
        if (SupportedRequestMethod.of(exchange.getRequestMethod()).map(r -> r == GET).orElse(false)) {
            exchange.getResponseBody().close();
        }
        exchange.close();
    }

    private void sendFile(@Nonnull HttpExchange exchange,
                          @Nonnull SupportedRequestMethod supportedRequestMethod,
                          @Nonnull Path path) throws IOException {
        File file = path.toFile();
        long responseLength = file.length();
        String hashCode = generateHashCode(file);
        exchange.getResponseHeaders().add(ETAG, hashCode);
        // the length  is also set for head requests (also expected by the RFC)
        // ignore the warning from ServerImpl
        String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file);
        exchange.getResponseHeaders().add(CONTENT_TYPE, contentType);
        exchange.getResponseHeaders().add(LAST_MODIFIED,
                getLastModifiedDate(file, ZoneId.of("GMT")).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        exchange.sendResponseHeaders(HTTP_OK, responseLength);
        if (supportedRequestMethod == GET) {
            Files.copy(path, exchange.getResponseBody());
            LOGGER.debug("File {} sent", file.toString());
        }
    }

    private void listFiles(@Nonnull HttpExchange exchange,
                           SupportedRequestMethod supportedRequestMethod,
                           @Nonnull Path absolutePath) throws IOException {
        // TODO render to HTML page
        Directory directory = Directory.listFiles(absolutePath);
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("files", directory.getFiles(true));
        String output = renderToHtml("/list_dir_template.vm", velocityContext);
        exchange.sendResponseHeaders(HTTP_OK, output.length());
        exchange.getResponseHeaders().add(CONTENT_TYPE, "text/html");
        // is GET
        if (supportedRequestMethod == GET) {
            exchange.getResponseBody().write(output.getBytes());
        }
        LOGGER.debug("Listed files in {}", absolutePath);
    }
}
