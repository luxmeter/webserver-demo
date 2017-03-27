package luxmeter;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import luxmeter.filter.IfMatchFilterWithoutStar;
import luxmeter.filter.IfNoneMatchFilterWithoutStar;
import luxmeter.filter.ModifiedSinceFilter;
import luxmeter.filter.RequestValidationFilter;
import luxmeter.handler.DefaultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private final Path rootDir;
    private final int port;

    public Server(Path rootDir, int port) {
        this.rootDir = rootDir;
        this.port = port;
    }

    public void start() throws IOException {
        // default implementation is already able to handle keep-alive requests
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        HttpHandler handler = new DefaultHandler(rootDir);
        // handles RequestExceptions and frees Input- and Output-streams
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
