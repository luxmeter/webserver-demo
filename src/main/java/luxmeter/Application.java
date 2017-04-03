package luxmeter;

import static org.apache.commons.cli.PatternOptionBuilder.NUMBER_VALUE;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;

import javax.annotation.Nonnull;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import luxmeter.filter.IfMatchFilterWith;
import luxmeter.filter.IfNoneMatchFilter;
import luxmeter.filter.ModifiedSinceFilter;
import luxmeter.filter.RequestValidationFilter;
import luxmeter.handler.DefaultHandler;

/**
 * Entry point of this application.
 * Provides a simple CLI interface for the user and starts the server.
 */
public class Application {
    private static final String OPT_ROOT_DIR = "rootdir";
    private static final String OPT_HELP = "help";
    private static final String OPT_PORT = "port";
    private static final int DEFAULT_PORT = 8080;
    private static final int EXIT_CODE_FAIL = 1;
    private static final int EXIT_CODE_SUCCESS = 0;

    public static void main(String[] args) throws IOException {
        configureJulLogger();

        CommandLineParser parser = new DefaultParser();
        Options options = createOptions();
        CommandLine line = null;
        int port = DEFAULT_PORT;

        try {
            line = parser.parse(options, args);
            port = getPort(line);
        } catch (ParseException e) {
            System.err.println("Can't understand what you want from me. Please take a look on the manual:");
            printHelpPage(options, EXIT_CODE_FAIL);
        }

        if (line.hasOption(OPT_HELP)) {
            printHelpPage(options, EXIT_CODE_SUCCESS);
        }

        Path rootDir = getRootDir(line);
        startServer(rootDir, port);
    }

    private static void printHelpPage(Options options, int exitCode) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar webserver.jar", options);
        System.exit(exitCode);
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
        context.getFilters().add(new IfNoneMatchFilter(rootDir));
        context.getFilters().add(new IfMatchFilterWith(rootDir));
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
