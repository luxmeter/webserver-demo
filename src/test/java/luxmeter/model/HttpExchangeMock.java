package luxmeter.model;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class HttpExchangeMock extends HttpExchange {
    private static final String PROTOCOL = "HTTP/1.1";
    private final String requestMethod;
    private final URI requestURI;
    private final Headers requestHeaders;
    private final Headers responseHeaders;
    private final ByteArrayOutputStream outputStream;
    private final ByteArrayInputStream inputStream;
    private int responseCode = -1;

    public HttpExchangeMock(URI requestURI, String requestMethod) {
        this.requestURI = requestURI;
        this.requestMethod = requestMethod;
        this.requestHeaders = new Headers();
        this.responseHeaders = new Headers();
        this.outputStream = new ByteArrayOutputStream();
        this.inputStream = new ByteArrayInputStream(StringUtils.EMPTY.getBytes());
    }

    @Override
    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public URI getRequestURI() {
        return requestURI;
    }

    @Override
    public String getRequestMethod() {
        return requestMethod;
    }

    @Override
    public HttpContext getHttpContext() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public InputStream getRequestBody() {
        return inputStream;
    }

    @Override
    public OutputStream getResponseBody() {
        return outputStream;
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        this.responseCode = rCode;
        this.getResponseHeaders().add("Content-Length", "" + responseLength);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return null;
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public void setAttribute(String name, Object value) {

    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {

    }

    @Override
    public HttpPrincipal getPrincipal() {
        return null;
    }

    public long getContentLength() {
        return Long.valueOf(getResponseHeaders().getFirst("Content-Length"));
    }

    public String responseBodyToString() {
        String result = new String(outputStream.toByteArray());
        try {
            if (getResponseHeaders().containsKey("Content-Type")) {
                String charset = getResponseHeaders().get("Content-Type").stream()
                        .filter(field -> field.startsWith("charset"))
                        .map(charsetField -> charsetField.replace("charset=", StringUtils.EMPTY))
                        .findFirst()
                        .orElse(null);
                if (isNotBlank(charset)) {
                    result = new String(outputStream.toByteArray(), charset);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<String> responseHeaderToList() {
        List<String> responseHeader = this.getResponseHeaders().entrySet().stream()
                .map(e -> String.format("%s: %s", e.getKey(), e.getValue().stream().collect(joining(" "))))
                .sorted()
                .collect(Collectors.toList());
        return Collections.unmodifiableList(responseHeader);
    }

}
