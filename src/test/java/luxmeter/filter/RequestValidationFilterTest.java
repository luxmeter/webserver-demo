package luxmeter.filter;

import luxmeter.model.HttpExchangeMock;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertThat;

public class RequestValidationFilterTest {
    private RequestValidationFilter testUnit = new RequestValidationFilter(Paths.get(System.getProperty("user.dir")));

    @Test
    public void shouldReturnNotFound() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/res_does_not_exist"), "GET");
        testUnit.doFilter(httpExchange, null);
        assertThat(httpExchange.getResponseCode(), equalTo(HttpURLConnection.HTTP_NOT_FOUND));
        assertThat(httpExchange.responseBodyToString(), isEmptyOrNullString());
    }
    @Test
    public void shouldReturnBadRequest() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "NOT_SUPPORTED_REQUEST");
        testUnit.doFilter(httpExchange, null);
        assertThat(httpExchange.getResponseCode(), equalTo(HttpURLConnection.HTTP_BAD_METHOD));
        assertThat(httpExchange.responseBodyToString(), isEmptyOrNullString());
    }
}
