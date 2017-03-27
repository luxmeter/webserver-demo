package luxmeter.filter;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static luxmeter.Util.generateHashCode;

/**
 * Enables the {@link IfMatchFilterWithoutStar} and {@link IfNoneMatchFilterWithoutStar} to share etag matching logic.
 */
interface EtagMatcher {
    /**
     * Checks if the etag of the requested resource matches any of the given etags.
     * Can be used by If-Match and If-None-Match header-field filterer.
     * @param file the requested file having once the passed etags associated with
     * @param etags etags from the if-match or if-none-match header field
     * @return the etag of the resource if it was not found within the list of etags
     */
    default String anyEtagMatches(File file, List<String> etags) {
        String hashCode = generateHashCode(file);

        boolean someEtagMatched = etags.stream()
                .anyMatch(requestedHashCode -> hashCode != null && Objects.equals(requestedHashCode, hashCode));

        return someEtagMatched ? null : hashCode;
    }
}
