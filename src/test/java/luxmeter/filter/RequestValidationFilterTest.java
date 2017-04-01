package luxmeter.filter;

import static java.net.HttpURLConnection.HTTP_BAD_METHOD;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static luxmeter.Util.NO_RESONSE_RETURNED_YET;
import static luxmeter.filter.RequestValidationFilter.ERROR_MSG_RESOURCE_NOT_FOUND;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.junit.Test;

import luxmeter.model.HttpExchangeMock;

public class RequestValidationFilterTest extends AbstractFilterTest {
    private final RequestValidationFilter testUnit = new RequestValidationFilter(Paths.get(System.getProperty("user.dir")));

    @Test
    public void shouldReturnNotFound() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/res_does_not_exist"), "GET");
        testUnit.doFilter(httpExchange, getMockedChain());
        checkResponseCodeAndChaining(httpExchange.getResponseCode(), HTTP_NOT_FOUND);
        assertThat(httpExchange.responseBodyToString(), endsWith(ERROR_MSG_RESOURCE_NOT_FOUND));
    }

    @Test
    public void shouldReturnBadRequest() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/some_file.md"), "NOT_SUPPORTED_REQUEST");
        testUnit.doFilter(httpExchange, getMockedChain());
        checkResponseCodeAndChaining(httpExchange.getResponseCode(), HTTP_BAD_METHOD);
        assertThat(httpExchange.responseBodyToString(),
                endsWith(RequestValidationFilter.ERROR_MSG_NOT_SUPPORTED_REQUEST));
    }

    @Test
    public void shouldPassValidation() throws IOException {
        HttpExchangeMock httpExchange = new HttpExchangeMock(
                URI.create("http://localhost:8080/src/test/resources/some_file.md"), "GET");
        testUnit.doFilter(httpExchange, getMockedChain());
        checkResponseCodeAndChaining(httpExchange.getResponseCode(), NO_RESONSE_RETURNED_YET);
    }
}
