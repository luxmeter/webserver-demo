package luxmeter;

import com.sun.net.httpserver.HttpHandler;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.assertThat;

public class DefaultHandlerTest {
    private HttpHandler testUnit = ContextManager.decorate(
            new DefaultHandler(Paths.get(System.getProperty("user.dir")).resolve("src/test/resources")));

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldListFilesRecursively() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080"), "GET");
        testUnit.handle(httpExchange);
        String responseBody = httpExchange.responseBodyToString();

        assertThat(httpExchange.responseHeaderToList(),
                contains(equalToIgnoringCase("Content-Length: 39")));
        assertThat(responseBody, equalTo(
                "some_file.md\n" +
                        "some_dir/another_file.txt\n"));
    }

    @Test
    public void shouldServeStaticFile() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        testUnit.handle(httpExchange);
        checkHeader(httpExchange);
        checkBody(httpExchange);
    }

    @Test
    public void shouldReturnHeadOnly() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "HEAD");
        testUnit.handle(httpExchange);
        checkHeader(httpExchange);
    }

    @Test
    public void shouldReturnEtag() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        testUnit.handle(httpExchange);
        assertThat(httpExchange.responseHeaderToList(), hasItem("Etag: ECCD66D6803584426248217359708D8C"));
    }

    private void checkBody(HttpExchangeMock httpExchange) {
        String responseBody = httpExchange.responseBodyToString();
        assertThat(responseBody, equalTo("Hello world!"));
    }

    private void checkHeader(HttpExchangeMock httpExchange) {
        List<String> responseHeader = httpExchange.responseHeaderToList();

        // TODO fix this behaviour of HttpServer
        String length = Objects.equals(httpExchange.getRequestMethod(), "GET") ? "12" : "12 -1";

        // notice that the mime-type is also checked that can be configured via mime.types
        assertThat(responseHeader, contains(
                equalToIgnoringCase("Content-Length: " + length),
                equalToIgnoringCase("Content-Type: text/markdown"),
                equalToIgnoringCase("Etag: ECCD66D6803584426248217359708D8C")));
        assertThat(httpExchange.getResponseCode(), Matchers.equalTo(HttpURLConnection.HTTP_OK));
    }
}