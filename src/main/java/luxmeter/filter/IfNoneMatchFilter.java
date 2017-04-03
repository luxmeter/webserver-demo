package luxmeter.filter;

import static luxmeter.Util.NO_BODY_CONTENT;
import static luxmeter.model.HeaderFieldContants.IF_NONE_MATCH;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;

import com.sun.net.httpserver.HttpExchange;

/**
 * Filter for the If-None-Match header-field.
 *
 * If any of the provided etags match a `NOT MODIFIED` is returned.
 */
public class IfNoneMatchFilter extends AbstractMatchFilter {
    public IfNoneMatchFilter(Path rootDir) {
        super(rootDir, IF_NONE_MATCH);
    }

    @Override
    protected boolean shouldContinue(HttpExchange exchange, String resourceEtag, boolean someEtagMatched)
            throws IOException {
        if (!someEtagMatched) {
            return true;
        }
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, NO_BODY_CONTENT);
        return false;
    }
}
