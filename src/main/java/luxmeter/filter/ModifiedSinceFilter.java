package luxmeter.filter;

import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static luxmeter.Util.*;
import static luxmeter.model.HeaderFieldContants.IF_MODIFIED_SINCE;
import static luxmeter.model.HeaderFieldContants.IF_NONE_MATCH;

public class ModifiedSinceFilter extends AbstractFilter {
    public ModifiedSinceFilter(Path rootDir) {
        super(rootDir);
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        // RCF: A recipient MUST ignore If-Modified-Since if the request contains an
        // If-None-Match header field [...]
        if (exchange.getResponseHeaders().containsKey(IF_NONE_MATCH)) {
            continueChain(exchange, chain);
            return;
        }

        Optional<String> modifiedSince = getHeaderFieldValues(
                exchange.getRequestHeaders(), IF_MODIFIED_SINCE).stream().findFirst();
        if (modifiedSince.isPresent()) {
            Path absolutePath = getAbsoluteSystemPath(rootDir, exchange.getRequestURI());
            File fileOrDirectory = absolutePath.toFile();

            // http://stackoverflow.com/questions/1930158/how-to-parse-date-from-http-last-modified-header
            ZonedDateTime dateOfInterest = ZonedDateTime.parse(modifiedSince.get(), DateTimeFormatter.RFC_1123_DATE_TIME);
            Instant lastModification = Instant.ofEpochMilli(fileOrDirectory.lastModified());
            if (lastModification.isAfter(dateOfInterest.toInstant())) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, NO_BODY_CONTENT);
            }
            else {
                continueChain(exchange, chain);
            }
        }
        else {
            continueChain(exchange, chain);
        }
    }
}
