package luxmeter.filter;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;

import static luxmeter.Util.NO_BODY_CONTENT;
import static luxmeter.Util.getHeaderFieldValues;
import static luxmeter.model.HeaderFieldContants.IF_NONE_MATCH;

// star operator -> resource must not exist
// but since this demo only serves static files a 404 error is raised beforehand
// which makes the star-operator obsolete
public class IfNoneMatchFilterWithoutStar extends AbstractFilter implements EtagMatcher{
    public IfNoneMatchFilterWithoutStar(Path rootDir) {
        super(rootDir);
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String newEtag = anyEtagMatches(rootDir, exchange.getRequestURI(),
                getHeaderFieldValues(exchange.getRequestHeaders(), IF_NONE_MATCH));
        if (newEtag == null) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, NO_BODY_CONTENT);
        } else {
            continueChain(exchange, chain);
        }
    }
}
