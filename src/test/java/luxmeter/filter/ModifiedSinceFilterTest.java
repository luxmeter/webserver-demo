package luxmeter.filter;

import luxmeter.model.HttpExchangeMock;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Paths;

import static luxmeter.Util.NO_ACTIONS_TAKEN;
import static luxmeter.model.HeaderFieldContants.IF_MODIFIED_SINCE;
import static luxmeter.model.HeaderFieldContants.IF_NONE_MATCH;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ModifiedSinceFilterTest {
    private ModifiedSinceFilter testUnit = new ModifiedSinceFilter(Paths.get(System.getProperty("user.dir")).resolve("src/test/resources"));

    @Test
    public void shouldReturnNotModified() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        // the file was definitely created after :D
        httpExchange.getRequestHeaders().remove(IF_NONE_MATCH);
        httpExchange.getRequestHeaders().add(IF_MODIFIED_SINCE, "Wed, 21 Oct 2015 07:28:00 GMT");
        testUnit.doFilter(httpExchange, null);
        assertThat(httpExchange.getResponseCode(), equalTo(HttpURLConnection.HTTP_NOT_MODIFIED));
    }

    @Test
    // should not filter because the modified-since date lies in the future
    // thus, the request can be processed
    public void shouldNotFilterAtAll() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        // the file was definitely created before :D
        httpExchange.getRequestHeaders().remove(IF_NONE_MATCH);
        httpExchange.getRequestHeaders().add(IF_MODIFIED_SINCE, "Wed, 21 Oct 2099 07:28:00 GMT");
        testUnit.doFilter(httpExchange, null);
        assertThat(httpExchange.getResponseCode(), equalTo(NO_ACTIONS_TAKEN));
    }
}
