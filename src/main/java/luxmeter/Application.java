package luxmeter;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import luxmeter.filter.IfMatchFilterWithoutStar;
import luxmeter.filter.IfNoneMatchFilterWithoutStar;
import luxmeter.filter.ModifiedSinceFilter;
import luxmeter.filter.RequestValidationFilter;
import luxmeter.handler.DefaultHandler;
import org.apache.commons.cli.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;

import static org.apache.commons.cli.PatternOptionBuilder.NUMBER_VALUE;

/**
 * Entry point of this application.
 * Provides a simple CLI interface for the user and starts the server.
 */
public class Application {
    private static final String OPT_ROOT_DIR = "rootdir";
    private static final String OPT_HELP = "help";
    private static final String OPT_PORT = "port";
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws IOException, ParseException {
        configureJulLogger();

        CommandLineParser parser = new DefaultParser();
        Options options = createOptions();
        CommandLine line = parser.parse(options, args);

        // validate that block-size has been set
        if (line.hasOption(OPT_HELP)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar webserver.jar", options);
            System.exit(0);
        }

        Path rootDir = getRootDir(line);
        int port = getPort(line);
        startServer(rootDir, port);
    }

    private static void startServer(@Nonnull Path rootDir, int port) throws IOException {
        System.out.println("Root Directory: " + rootDir);
        System.out.println(String.format("Ready to serve: %s\n", "http://localhost:"+port));

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

    private static Options createOptions() {
        // create the Options
        Options options = new Options();
        options.addOption(Option.builder()
                .longOpt(OPT_ROOT_DIR)
                .desc("The root directory of the server (default cwd).")
                .hasArg()
                .argName("DIR")
                .build());
        options.addOption(Option.builder("p")
                .longOpt(OPT_PORT)
                .desc("Port of this server (default 8080).")
                .hasArg()
                .argName(OPT_PORT.toUpperCase())
                .type(NUMBER_VALUE)
                .build());
        options.addOption("h", OPT_HELP, false, "Shows this help page.");
        return options;
    }

    private static Path getRootDir(@Nonnull CommandLine line) {
        Path rootDir = Paths.get(line.getOptionValue(OPT_ROOT_DIR, System.getProperty("user.dir")));
        if (!rootDir.toFile().isDirectory()) {
             System.err.println(String.format("Don\'t fool me. %s does not exist.", rootDir.toAbsolutePath()));
            System.exit(1);
        }
        return rootDir;
    }

    private static int getPort(@Nonnull CommandLine line) throws ParseException {
        Number number = (Number) line.getParsedOptionValue(OPT_PORT);
        int port = DEFAULT_PORT;
        if (number != null) {
            port = Integer.valueOf("" + number);
        }

        return port;
    }

    private static void configureJulLogger() throws IOException {
        InputStream config = Application.class.getResourceAsStream("/jul-logger.properties");
        LogManager.getLogManager().readConfiguration(config);
    }
}
