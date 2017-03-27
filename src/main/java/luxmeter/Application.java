package luxmeter;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.logging.LogManager;

import static java.util.Arrays.asList;
import static org.apache.commons.cli.PatternOptionBuilder.NUMBER_VALUE;

public class Application {
    private static final String HELP_FILE = "/help.txt";
    private static final String OPT_ROOT_DIR = "--rootdir";
    private static final String OPT_HELP = "--help";
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final HashSet<String> ALLOWED_OPTIONS = new HashSet<>(asList(OPT_ROOT_DIR, OPT_HELP));

    public static void main(String[] args) throws IOException, ParseException {
        configureJulLogger();

        CommandLineParser parser = new DefaultParser();
        Options options = createOptions();
        CommandLine line = parser.parse(options, args);

        // validate that block-size has been set
        if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar webserver.jar", options);
            System.exit(0);
        }

        Path rootDir = getRootDir(line);
        int port = getPort(line);
        System.out.println("Root Directory: " + rootDir);
        System.out.println(String.format("Ready to serve: %s\n", "http://localhost:"+port));
        new Server(rootDir, port).start();
    }

    private static Options createOptions() {
        // create the Options
        Options options = new Options();
        options.addOption(Option.builder()
                .longOpt("rootdir")
                .desc("The root directory of the server (default cwd).")
                .hasArg()
                .argName("DIR")
                .build());
        options.addOption(Option.builder("p")
                .longOpt("port")
                .desc("Port of this server (default 8080).")
                .hasArg()
                .argName("PORT")
                .type(NUMBER_VALUE)
                .build());
        options.addOption("h", "help", false, "Shows this help page.");
        return options;
    }

    private static Path getRootDir(CommandLine line) {
        Path rootDir = Paths.get(line.getOptionValue("rootdir"));
        if (!rootDir.toFile().isDirectory()) {
             System.err.println(String.format("Don\'t fool me. %s does not exist.", rootDir.toAbsolutePath()));
            System.exit(1);
        }
        return rootDir;
    }

    private static int getPort(CommandLine line) throws ParseException {
        Number number = (Number) line.getParsedOptionValue("port");
        int port = 8080;
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
