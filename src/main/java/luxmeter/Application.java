package luxmeter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Application {
    private static final String HELP_FILE = "/help.txt";
    private static final String OPT_ROOT_DIR = "--rootdir";
    private static final String OPT_HELP = "--help";
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final HashSet<String> ALLOWED_OPTIONS = new HashSet<>(asList(OPT_ROOT_DIR, OPT_HELP));

    public static void main(String[] args) throws IOException {
        List<String> options = checkArguments(args);
        Path rootDir = getRootDir(options);
        System.out.println("Root Directory: " + rootDir);
        System.out.println(String.format("Ready to serve: %s\n", "http://localhost:8080/"));
        new Server(rootDir).start();
    }

    private static List<String> checkArguments(String[] args) throws IOException {
        List<String> arguments = asList(args);
        Set<String> options = arguments.stream()
                // make '--rootDir=something' to '--rootDir'
                // or something invalid --> in this case the help page is presented
                .map(arg -> arg.trim().replaceAll("(--.*)=.*", "$1"))
                .collect(Collectors.toSet());
        if (!arguments.isEmpty()
                && (arguments.contains(OPT_HELP) || !ALLOWED_OPTIONS.containsAll(options))) {
            printHelpPage();
            System.exit(1);
        }
        return arguments;
    }

    private static Path getRootDir(Collection<String> arguments) {
        Path path = arguments.stream()
                .filter(option -> option.startsWith(OPT_ROOT_DIR))
                .map(option -> option.substring(OPT_ROOT_DIR.length() + 1))
                // in case someone wrote --rootDir='something'
                .map(option -> option.replaceAll("['|\"]", StringUtils.EMPTY))
                .map(Paths::get)
                .findFirst()
                .orElse(Paths.get(System.getProperty("user.dir")));
        if (!path.toFile().isDirectory()) {
            System.err.println("Don\'t fool me. Enter an existing directory");
            System.exit(1);
        }
        return path;
    }

    private static void printHelpPage() throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(Application.class.getResourceAsStream(HELP_FILE)));
        StringBuilder output = new StringBuilder();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            output.append(line + "\n");
        }
        System.out.println(output.toString());
    }

}
