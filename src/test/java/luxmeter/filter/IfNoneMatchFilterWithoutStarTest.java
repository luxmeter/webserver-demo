package luxmeter.filter;

import luxmeter.model.HttpExchangeMock;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Paths;

import static luxmeter.model.HeaderFieldContants.IF_NONE_MATCH;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class IfNoneMatchFilterWithoutStarTest {

    private IfNoneMatchFilterWithoutStar testUnit = new IfNoneMatchFilterWithoutStar(
            Paths.get(System.getProperty("user.dir")).resolve("src/test/resources"));

    @Test
    public void shouldNotSendSameFileTwice() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        httpExchange.getRequestHeaders().add(IF_NONE_MATCH, "ECCD66D6803584426248217359708D8C");
        testUnit.doFilter(httpExchange, null);
        assertThat(httpExchange.getResponseCode(), equalTo(HttpURLConnection.HTTP_NOT_MODIFIED));
    }
}
