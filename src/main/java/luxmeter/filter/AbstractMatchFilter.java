package luxmeter.filter;

import static luxmeter.Util.generateHashCode;
import static luxmeter.Util.getAbsoluteSystemPath;
import static luxmeter.Util.getHeaderFieldValues;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.sun.net.httpserver.HttpExchange;

/**
 * Abstract class for the etag checking header-fields, e.g. If-Match.
 * Subclasses only need to define in which case the processing of the request should continue.
 * If it should be canceled, an appropriate header response needs to be sent.
 *
 */
public abstract class AbstractMatchFilter extends AbstractFilter {
    public static final String STAR_OPERATOR = "*";
    private final String headerField;

    public AbstractMatchFilter(Path rootDir, String headerField) {
        super(rootDir);
        this.headerField = headerField;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        List<String> etags = getHeaderFieldValues(exchange.getRequestHeaders(), headerField);
        Path absolutePath = getAbsoluteSystemPath(getRootDir(), exchange.getRequestURI());
        File file = absolutePath.toFile();

        boolean continueProcessing = etags.isEmpty();
        if (!etags.isEmpty()) {
            String resourceEtag = generateHashCode(file);

            boolean someEtagMatched =
                    etags.stream().anyMatch(requestedEtag -> etagMatches(requestedEtag, resourceEtag));

            continueProcessing = shouldContinue(exchange, resourceEtag, someEtagMatched);
        }

        if (continueProcessing) {
            continueProcessing(exchange, chain);
        }
    }

    private boolean etagMatches(String requestedEtag, String resourceEtag) {
        return resourceEtag != null
                && (requestedEtag.equals(STAR_OPERATOR) || Objects.equals(requestedEtag, resourceEtag));
    }

    /**
     *
     * @param exchange for further inspect the request and/or send an appropriate on header response on mismatch
     * @param resourceEtag current etag of the resource
     * @param someEtagMatched true if any of the client provided etags matched with the current etag
     * @return true if the processing of the request should be continued
     * @throws IOException
     */
    protected abstract boolean shouldContinue(HttpExchange exchange, String resourceEtag,
            boolean someEtagMatched)  throws IOException;
}
