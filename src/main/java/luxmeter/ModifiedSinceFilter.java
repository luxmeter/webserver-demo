package luxmeter;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static luxmeter.HeaderFieldContants.IF_MODIFIED_SINCE;
import static luxmeter.Util.*;

public class ModifiedSinceFilter extends Filter {
    private final Path rootDir;

    public ModifiedSinceFilter(Path rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
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
        }
    }

    @Override
    public String description() {
        return getClass().getName();
    }
}
