package luxmeter.filter;

import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;

import static luxmeter.Util.NO_BODY_CONTENT;
import static luxmeter.Util.getAbsoluteSystemPath;
import static luxmeter.Util.getHeaderFieldValues;
import static luxmeter.model.HeaderFieldContants.IF_NONE_MATCH;

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
            String newEtag = anyEtagMatches(file,
                    getHeaderFieldValues(exchange.getRequestHeaders(), IF_NONE_MATCH));
            if (newEtag == null) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, NO_BODY_CONTENT);
            } else {
                continueChain(exchange, chain);
            }
        }
        else {
            continueChain(exchange, chain);
        }
    }
}
