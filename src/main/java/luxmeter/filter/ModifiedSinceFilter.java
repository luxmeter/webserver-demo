package luxmeter.filter;

import static luxmeter.Util.NO_BODY_CONTENT;
import static luxmeter.Util.getAbsoluteSystemPath;
import static luxmeter.Util.getHeaderFieldValues;
import static luxmeter.model.HeaderFieldContants.IF_MODIFIED_SINCE;
import static luxmeter.model.HeaderFieldContants.IF_NONE_MATCH;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;

/**
 * Filter for the If-Modified-Since header-field.
 *
 * If the requested resource has NOT been modified since the last time the client saw it,
 * `NOT MODIFIED` is returned.
 */
public class ModifiedSinceFilter extends AbstractFilter {
    public ModifiedSinceFilter(Path rootDir) {
        super(rootDir);
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        // RCF: A recipient MUST ignore If-Modified-Since if the request contains an
        // If-None-Match header field [...]
        if (!exchange.getResponseHeaders().containsKey(IF_NONE_MATCH)) {
            Optional<String> modifiedSince = getHeaderFieldValues(
                    exchange.getRequestHeaders(), IF_MODIFIED_SINCE).stream().findFirst();
            if (modifiedSince.isPresent()) {
                Path absolutePath = getAbsoluteSystemPath(getRootDir(), exchange.getRequestURI());
                File fileOrDirectory = absolutePath.toFile();

                // http://stackoverflow.com/questions/1930158/how-to-parse-date-from-http-last-modified-header
                ZonedDateTime dateOfInterest = ZonedDateTime
                        .parse(modifiedSince.get(), DateTimeFormatter.RFC_1123_DATE_TIME);
                Instant lastModification = Instant.ofEpochMilli(fileOrDirectory.lastModified());
                if (!lastModification.isAfter(dateOfInterest.toInstant())) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, NO_BODY_CONTENT);
                    return;
                }
            }
        }

        continueProcessing(exchange, chain);
    }
}
