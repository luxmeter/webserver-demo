package luxmeter.filter;

import static luxmeter.Util.NO_BODY_CONTENT;
import static luxmeter.Util.getAbsoluteSystemPath;
import static luxmeter.Util.getHeaderFieldValues;
import static luxmeter.model.HeaderFieldContants.IF_NONE_MATCH;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;

/**
 * Filter for the If-None-Match header-field.
 * Notice that the star-operator is obsolete in this demo
 * since server only serves existing static files.
 *
 * If any of the provided etags match a `NOT MODIFIED` is returned.
 */
public class IfNoneMatchFilterWithoutStar extends AbstractFilter implements EtagMatcher{
    public IfNoneMatchFilterWithoutStar(Path rootDir) {
        super(rootDir);
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        Path absolutePath = getAbsoluteSystemPath(getRootDir(), exchange.getRequestURI());
        File file = absolutePath.toFile();

        if (file.isFile()) {
            List<String> etags = getHeaderFieldValues(exchange.getRequestHeaders(), IF_NONE_MATCH);
            Optional<String> newEtag = anyEtagMatches(file, etags);
            boolean existingEtagMatched = !newEtag.isPresent();
            if (existingEtagMatched) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, NO_BODY_CONTENT);
                return; // interrupt chaining
            }
        }

        continueProcessing(exchange, chain);
    }
}
