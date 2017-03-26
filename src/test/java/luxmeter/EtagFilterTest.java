package luxmeter;

import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class EtagFilterTest {
    private EtagFilter testUnit = new EtagFilter(Paths.get(System.getProperty("user.dir")).resolve("src/test/resources"));

    @Test
    public void shouldNotSendSameFileTwice() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        httpExchange.getRequestHeaders().add("If-none-match", "ECCD66D6803584426248217359708D8C");
        testUnit.doFilter(httpExchange, null);
        assertThat(httpExchange.getResponseCode(), equalTo(HttpURLConnection.HTTP_NOT_MODIFIED));
    }
}
