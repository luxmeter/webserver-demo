package luxmeter.filter;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static luxmeter.Util.generateHashCode;
import static luxmeter.Util.getAbsoluteSystemPath;

/**
 * Enables the {@link IfMatchFilterWithoutStar} and {@link IfNoneMatchFilterWithoutStar} to share etag matching logic.
 */
interface EtagMatcher {
    /**
     * Checks if the etag of the requested resource matches any of the given etags.
     * Can be used by If-Match and If-None-Match header-field filterer.
     * @param rootDir the root dir
     * @param requestUri the requested url
     * @param etags etags from the if-match or if-none-match header field
     * @return the etag of the resource if it was not found within the list of etags
     */
    default String anyEtagMatches(Path rootDir, URI requestUri, List<String> etags) {
        Path absolutePath = getAbsoluteSystemPath(rootDir, requestUri);
        File fileOrDirectory = absolutePath.toFile();
        String hashCode = generateHashCode(fileOrDirectory);

        boolean someEtagMatched = etags.stream()
                .anyMatch(requestedHashCode -> hashCode != null && Objects.equals(requestedHashCode, hashCode));

        return someEtagMatched ? null : hashCode;
    }
}
