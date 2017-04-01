package luxmeter.filter;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static luxmeter.Util.NO_RESONSE_RETURNED_YET;
import static luxmeter.model.HeaderFieldContants.IF_MODIFIED_SINCE;
import static luxmeter.model.HeaderFieldContants.IF_NONE_MATCH;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.junit.Test;

import luxmeter.model.HttpExchangeMock;

public class ModifiedSinceFilterTest extends AbstractFilterTest {
    private final ModifiedSinceFilter testUnit = new ModifiedSinceFilter(Paths.get(System.getProperty("user.dir")).resolve("src/test/resources"));

    @Test
    public void shouldReturnNotModified() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        // the file was definitely created before :D
        httpExchange.getRequestHeaders().remove(IF_NONE_MATCH); // server prefers etags to dates
        httpExchange.getRequestHeaders().add(IF_MODIFIED_SINCE, "Wed, 21 Oct 2099 07:28:00 GMT");
        testUnit.doFilter(httpExchange, getMockedChain());
        checkResponseCodeAndChaining(httpExchange.getResponseCode(), HTTP_NOT_MODIFIED);
    }

    @Test
    // should not filter because the modified-since date lies in the future
    // thus, the request can be processed
    public void shouldNotFilterAtAll() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        // the file was definitely created after :D
        httpExchange.getRequestHeaders().remove(IF_NONE_MATCH);
        httpExchange.getRequestHeaders().add(IF_MODIFIED_SINCE, "Wed, 21 Oct 2015 07:28:00 GMT");
        testUnit.doFilter(httpExchange, getMockedChain());
        checkResponseCodeAndChaining(httpExchange.getResponseCode(), NO_RESONSE_RETURNED_YET);
    }
}
