package luxmeter.handler;

import static luxmeter.model.Constants.ETAG_SOME_FILE;
import static luxmeter.model.HeaderFieldContants.ETAG;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.sun.net.httpserver.HttpHandler;
import luxmeter.model.HttpExchangeMock;

public class DefaultHandlerTest {
    private final HttpHandler testUnit = new DefaultHandler(
            Paths.get(System.getProperty("user.dir")).resolve("src/test/resources"));

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
        assertThat(httpExchange.responseHeaderToList(), hasItem(ETAG + ": " + ETAG_SOME_FILE));
    }

    private void checkBody(HttpExchangeMock httpExchange) {
        String responseBody = httpExchange.responseBodyToString();
        assertThat(responseBody, equalTo("Hello world!"));
    }

    private void checkHeader(HttpExchangeMock httpExchange) {
        List<String> responseHeader = httpExchange.responseHeaderToList();

        // notice that the mime-type is also checked that can be configured via mime.types
        assertThat(responseHeader, containsInAnyOrder(
                equalToIgnoringCase("Last-modified: Sun, 26 Mar 2017 10:44:33 GMT"),
                equalToIgnoringCase("Content-Length: 12"),
                equalToIgnoringCase("Content-Type: text/markdown"),
                equalToIgnoringCase(ETAG + ": " + ETAG_SOME_FILE)));
        assertThat(httpExchange.getResponseCode(), Matchers.equalTo(HttpURLConnection.HTTP_OK));
    }
}
