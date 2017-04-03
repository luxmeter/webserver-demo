package luxmeter.filter;

import static luxmeter.Util.NO_BODY_CONTENT;
import static luxmeter.model.HeaderFieldContants.ETAG;
import static luxmeter.model.HeaderFieldContants.IF_MATCH;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;

import com.sun.net.httpserver.HttpExchange;


/**
 * Filter for the If-Match header-field.
 *
 * If none of the provided etags match a `PRECONDITION FAILED` is returned.
 */
public class IfMatchFilterWith extends AbstractMatchFilter {
    public IfMatchFilterWith(Path rootDir) {
        super(rootDir, IF_MATCH);
    }

    protected boolean shouldContinue(HttpExchange exchange, String hashCode, boolean someEtagMatched)  throws IOException {
        if (someEtagMatched) {
            return true;
        }
        else if (hashCode != null) {
            exchange.getResponseHeaders().add(ETAG, hashCode);
        }
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_PRECON_FAILED, NO_BODY_CONTENT);
        return false;
    }
}
