package luxmeter;

import com.sun.net.httpserver.HttpHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
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
    public void shouldReturnNotFound() throws IOException {
        exception.expect(RequestException.class);
        exception.expectMessage("" + HttpURLConnection.HTTP_NOT_FOUND);

        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/res_does_not_exist"), "GET");
        testUnit.handle(httpExchange);
        checkHeader(httpExchange);
        checkBody(httpExchange);
    }

    @Test
    public void shouldReturnBadRequest() throws IOException {
        exception.expect(RequestException.class);
        exception.expectMessage("" + HttpURLConnection.HTTP_BAD_REQUEST);

        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "NOT_SUPPORTED_REQUEST");
        testUnit.handle(httpExchange);
        checkHeader(httpExchange);
        checkBody(httpExchange);
    }

    @Test
    public void shouldShowHeader() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "GET");
        testUnit.handle(httpExchange);
        checkHeader(httpExchange);
    }

    private void checkBody(HttpExchangeMock httpExchange) {
        String responseBody = httpExchange.responseBodyToString();
        assertThat(responseBody, equalTo("Hello world!"));
    }

    private void checkHeader(HttpExchangeMock httpExchange) {
        List<String> responseHeader = httpExchange.responseHeaderToList();
        // notice that the mime-type is also checked that can be configured via mime.types
        assertThat(responseHeader, contains(
                equalToIgnoringCase("Content-Length: 12"),
                equalToIgnoringCase("Content-Type: text/markdown")));
    }
}
