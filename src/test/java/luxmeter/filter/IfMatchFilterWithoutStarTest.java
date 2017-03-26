package luxmeter.filter;

import luxmeter.model.HttpExchangeMock;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Paths;

import static luxmeter.Util.NO_ACTIONS_TAKEN;
import static luxmeter.model.HeaderFieldContants.IF_MATCH;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class IfMatchFilterWithoutStarTest {
    private IfMatchFilterWithoutStar testUnit = new IfMatchFilterWithoutStar(
            Paths.get(System.getProperty("user.dir")).resolve("src/test/resources"));

    @Test
    // request a resource which etag has changed
    public void shouldReturnFailedPrecondition() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        httpExchange.getRequestHeaders().add(IF_MATCH, "abc");
        testUnit.doFilter(httpExchange, null);
        assertThat(httpExchange.getResponseCode(), equalTo(HttpURLConnection.HTTP_PRECON_FAILED));
    }

    @Test
    public void shouldPassThePrecondition() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        httpExchange.getRequestHeaders().add(IF_MATCH, "ECCD66D6803584426248217359708D8C");
        testUnit.doFilter(httpExchange, null);
        assertThat(httpExchange.getResponseCode(), equalTo(NO_ACTIONS_TAKEN));
    }
}
