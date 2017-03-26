package luxmeter.filter;

import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.List;

import static luxmeter.Util.NO_BODY_CONTENT;
import static luxmeter.Util.getAbsoluteSystemPath;
import static luxmeter.Util.getHeaderFieldValues;
import static luxmeter.model.HeaderFieldContants.ETAG;
import static luxmeter.model.HeaderFieldContants.IF_MATCH;


// star operator -> resource needs to exist
// but since this demo only serves static files a 404 error is raised beforehand
// which makes the star-operator obsolete
public class IfMatchFilterWithoutStar extends AbstractFilter implements EtagMatcher {
    public IfMatchFilterWithoutStar(Path rootDir) {
        super(rootDir);
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        List<String> etags = getHeaderFieldValues(exchange.getRequestHeaders(), IF_MATCH);
        Path absolutePath = getAbsoluteSystemPath(rootDir, exchange.getRequestURI());
        File file = absolutePath.toFile();

        if (!etags.isEmpty() && file.isFile()) {
            String newEtag = anyEtagMatches(file, etags);
            if (newEtag != null) {
                exchange.getResponseHeaders().add(ETAG, newEtag);
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_PRECON_FAILED, NO_BODY_CONTENT);
            } else {
                continueChain(exchange, chain);
            }
        }
        else {
            continueChain(exchange, chain);
        }
    }
}
