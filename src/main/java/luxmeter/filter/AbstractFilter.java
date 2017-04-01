package luxmeter.filter;

import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * Parent of all Filterer.
 */
public abstract class AbstractFilter extends Filter {
    private final Path rootDir;

    protected AbstractFilter(@Nonnull Path rootDir) {
        this.rootDir = rootDir;
    }


    protected void continueProcessing(HttpExchange exchange, Chain chain) throws IOException {
        if (chain != null) {
            chain.doFilter(exchange);
        }
    }

    @Override
    public String description() {
        return getClass().getName();
    }

    @Nonnull
    public Path getRootDir() {
        return rootDir;
    }
}
