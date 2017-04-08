package luxmeter.filter;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static luxmeter.model.Constants.ETAG_SOME_FILE;
import static luxmeter.model.HeaderFieldContants.IF_NONE_MATCH;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.junit.Test;

import luxmeter.model.HttpExchangeMock;

public class IfNoneMatchFilterTest extends AbstractFilterTest {

    private final IfNoneMatchFilter testUnit = new IfNoneMatchFilter(
            Paths.get(System.getProperty("user.dir")).resolve("src/test/resources"));

    @Test
    public void shouldNotSendSameFileTwice() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        httpExchange.getRequestHeaders().add(IF_NONE_MATCH, ETAG_SOME_FILE);
        testUnit.doFilter(httpExchange, getMockedChain());
        checkResponseCodeAndChaining(httpExchange.getResponseCode(), HTTP_NOT_MODIFIED);
    }

    @Test
    public void shouldCancelRequestWithStarOperatorAndExistingFile() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        httpExchange.getRequestHeaders().add(IF_NONE_MATCH, "*");
        testUnit.doFilter(httpExchange, getMockedChain());
        checkResponseCodeAndChaining(httpExchange.getResponseCode(), HTTP_NOT_MODIFIED);
    }
}
