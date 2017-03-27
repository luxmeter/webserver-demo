package luxmeter.filter;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Parent of all Filterer.
 */
public abstract class AbstractFilter extends Filter {
    private final Path rootDir;

    protected AbstractFilter(Path rootDir) {
        this.rootDir = rootDir;
    }


    protected void continueChain(HttpExchange exchange, Chain chain) throws IOException {
        if (chain != null) {
            chain.doFilter(exchange);
        }
    }

    @Override
    public String description() {
        return getClass().getName();
    }

    public Path getRootDir() {
        return rootDir;
    }
}
