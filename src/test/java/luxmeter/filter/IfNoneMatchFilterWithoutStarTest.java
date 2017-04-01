package luxmeter.filter;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static luxmeter.model.HeaderFieldContants.IF_NONE_MATCH;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.junit.Test;

import luxmeter.model.HttpExchangeMock;

public class IfNoneMatchFilterWithoutStarTest extends AbstractFilterTest {

    private final IfNoneMatchFilterWithoutStar testUnit = new IfNoneMatchFilterWithoutStar(
            Paths.get(System.getProperty("user.dir")).resolve("src/test/resources"));

    @Test
    public void shouldNotSendSameFileTwice() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        httpExchange.getRequestHeaders().add(IF_NONE_MATCH, "ECCD66D6803584426248217359708D8C");
        testUnit.doFilter(httpExchange, getMockedChain());
        checkResponseCodeAndChaining(httpExchange.getResponseCode(), HTTP_NOT_MODIFIED);
    }
}
