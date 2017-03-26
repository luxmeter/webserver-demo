package luxmeter;

import com.sun.net.httpserver.*;
import luxmeter.filter.EtagFilter;
import luxmeter.filter.RequestValidator;
import luxmeter.handler.ContextManager;
import luxmeter.handler.DefaultHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class Application {
    public static void main(String[] args) throws IOException {
        // default implementation is already able to handle keep-alive requests
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        Path rootDir = Paths.get(System.getProperty("user.dir"));
        HttpHandler handler = new DefaultHandler(rootDir);
        // handles RequestExceptions and frees Input- and Output-streams
        handler = ContextManager.decorate(handler);
        HttpContext context = server.createContext("/", handler);
        // TODO add spring injection to dynamically load filterer
        context.getFilters().add(new RequestValidator(rootDir));
        context.getFilters().add(new EtagFilter(rootDir));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

}
