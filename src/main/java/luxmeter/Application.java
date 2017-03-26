package luxmeter;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class Application {
    public static void main(String[] args) throws IOException {
        // default implementation is already able to handle keep-alive requests
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        HttpHandler handler = new DefaultHandler(Paths.get(System.getProperty("user.dir")));
        // handles RequestExceptions and frees Input- and Output-streams
        handler = ContextManager.decorate(handler);
        HttpContext context = server.createContext("/", handler);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }
}
