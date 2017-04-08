package luxmeter;

import static luxmeter.model.Constants.ETAG_SOME_FILE;
import static luxmeter.model.HeaderFieldContants.IF_MATCH;
import static luxmeter.model.HeaderFieldContants.IF_MODIFIED_SINCE;
import static luxmeter.model.HeaderFieldContants.IF_NONE_MATCH;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Paths;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.sun.net.httpserver.HttpServer;

public class ApplicationTest {
    private static HttpServer httpServer;
    private static String BASE_URL;
    private static int PORT;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setup() throws IOException {
        PORT = getFreePort();
        BASE_URL = "http://localhost:" + PORT + "/";
        httpServer = new Application().startServer(Paths.get("src/test/resources"), PORT);
    }

    private static int getFreePort() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        int freePort = serverSocket.getLocalPort();
        serverSocket.close();
        return freePort;
    }

    @AfterClass
    public static void cleanup() {
        httpServer.stop(0);
    }

    private String getResourceUrl(String pathToResource) {
        return BASE_URL + pathToResource;
    }

    private void checkResponse(String responseBody) {
        assertThat(responseBody, is(notNullValue()));
        assertThat(responseBody, equalTo("Hello world!"));
    }

    @Test
    public void shouldProcessSimpleGetRequest() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(getResourceUrl("some_file.md"));
        String responseBody = client.execute(request, new BasicResponseHandler());
        checkResponse(responseBody);
    }

    @Test
    public void sohuldFailIfNoneMatchCheck() throws IOException {
        expectedException.expect(HttpResponseException.class);
        expectedException.expectMessage("Not Modified");
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(getResourceUrl("some_file.md"));
        request.addHeader(IF_NONE_MATCH, ETAG_SOME_FILE);
        client.execute(request, new BasicResponseHandler());
    }

    @Test
    public void shouldPassIfNoneMatchCheck() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(getResourceUrl("some_file.md"));
        request.addHeader(IF_NONE_MATCH, "abc");
        String responseBody = client.execute(request, new BasicResponseHandler());
        checkResponse(responseBody);
    }

    @Test
    public void shouldPassIfMatchCheck() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(getResourceUrl("some_file.md"));
        request.addHeader(IF_MATCH, ETAG_SOME_FILE);
        client.execute(request, new BasicResponseHandler());
        String responseBody = client.execute(request, new BasicResponseHandler());
        checkResponse(responseBody);
    }

    @Test
    public void shouldFailIfMatchCheck() throws IOException {
        expectedException.expect(HttpResponseException.class);
        expectedException.expectMessage("Precondition Failed");
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(getResourceUrl("some_file.md"));
        request.addHeader(IF_MATCH, "abc");
        client.execute(request, new BasicResponseHandler());
    }


    @Test
    public void shouldPassIfModifiedSinceCheck() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(getResourceUrl("some_file.md"));
        request.addHeader(IF_MODIFIED_SINCE, "Wed, 02 Jul 2014 07:28:00 GMT");
        client.execute(request, new BasicResponseHandler());
        String responseBody = client.execute(request, new BasicResponseHandler());
        checkResponse(responseBody);
    }

    @Test
    public void shouldFailIfModifiedSinceCheck() throws IOException {
        expectedException.expect(HttpResponseException.class);
        expectedException.expectMessage("Not Modified");
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(getResourceUrl("some_file.md"));
        request.addHeader(IF_MODIFIED_SINCE, "Wed, 21 Oct 2099 07:28:00 GMT");
        client.execute(request, new BasicResponseHandler());
    }
}
