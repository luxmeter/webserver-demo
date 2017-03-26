package luxmeter;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import luxmeter.filter.IfMatchFilterWithoutStar;
import luxmeter.filter.IfNoneMatchFilterWithoutStar;
import luxmeter.filter.ModifiedSinceFilter;
import luxmeter.filter.RequestValidationFilter;
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
        context.getFilters().add(new RequestValidationFilter(rootDir));
        context.getFilters().add(new ModifiedSinceFilter(rootDir));
        context.getFilters().add(new IfNoneMatchFilterWithoutStar(rootDir));
        context.getFilters().add(new IfMatchFilterWithoutStar(rootDir));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

}
