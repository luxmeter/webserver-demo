package luxmeter.filter;

import static luxmeter.Util.NO_BODY_CONTENT;
import static luxmeter.Util.getAbsoluteSystemPath;
import static luxmeter.Util.getHeaderFieldValues;
import static luxmeter.model.HeaderFieldContants.ETAG;
import static luxmeter.model.HeaderFieldContants.IF_MATCH;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;


/**
 * Filter for the If-Match header-field.
 * Notice that the star-operator is obsolete in this demo
 * since server only serves existing static files.
 *
 * If none of the provided etags match a `PRECONDITION FAILED` is returned.
 */
public class IfMatchFilterWithoutStar extends AbstractFilter implements EtagMatcher {
    public IfMatchFilterWithoutStar(Path rootDir) {
        super(rootDir);
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        List<String> etags = getHeaderFieldValues(exchange.getRequestHeaders(), IF_MATCH);
        Path absolutePath = getAbsoluteSystemPath(getRootDir(), exchange.getRequestURI());
        File file = absolutePath.toFile();

        if (!etags.isEmpty() && file.isFile()) {
            Optional<String> newEtag = anyEtagMatches(file, etags);
            boolean noExistingEtagMatched = newEtag.isPresent();
            if (noExistingEtagMatched) {
                exchange.getResponseHeaders().add(ETAG, newEtag.get());
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_PRECON_FAILED, NO_BODY_CONTENT);
                return; // interrupt chaining
            }
        }

        continueProcessing(exchange, chain);
    }
}
