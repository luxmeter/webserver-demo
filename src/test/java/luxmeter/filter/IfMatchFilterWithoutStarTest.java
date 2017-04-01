package luxmeter.filter;

import static java.net.HttpURLConnection.HTTP_PRECON_FAILED;
import static luxmeter.Util.NO_RESONSE_RETURNED_YET;
import static luxmeter.model.HeaderFieldContants.IF_MATCH;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.junit.Test;

import luxmeter.model.HttpExchangeMock;

public class IfMatchFilterWithoutStarTest extends AbstractFilterTest {
    private final IfMatchFilterWithoutStar testUnit = new IfMatchFilterWithoutStar(
            Paths.get(System.getProperty("user.dir")).resolve("src/test/resources"));

    @Test
    // request a resource which etag has changed
    public void shouldReturnFailedPrecondition() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        httpExchange.getRequestHeaders().add(IF_MATCH, "abc");
        testUnit.doFilter(httpExchange, getMockedChain());
        checkResponseCodeAndChaining(httpExchange.getResponseCode(), HTTP_PRECON_FAILED);
    }

    @Test
    public void shouldPassThePrecondition() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        httpExchange.getRequestHeaders().add(IF_MATCH, "ECCD66D6803584426248217359708D8C");
        testUnit.doFilter(httpExchange, getMockedChain());
        checkResponseCodeAndChaining(httpExchange.getResponseCode(), NO_RESONSE_RETURNED_YET);

    }
}
