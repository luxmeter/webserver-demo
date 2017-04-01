package luxmeter.filter;

import static luxmeter.Util.generateHashCode;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A mixin interface enabling the {@link IfMatchFilterWithoutStar}
 * and {@link IfNoneMatchFilterWithoutStar} to share etag matching logic.
 */
 // Another approach would have been an abstract class or static util method.
 // However, the mixins have some benefits:
 //     * Mixins are easier to use since the target class has most probably already a class from which it inherits
 //     * Mixins makes it is harder to break user code since no state is shared
interface EtagMatcher {
    /**
     * Checks if the etag of the requested resource matches any of the given etags.
     * Can be used by If-Match and If-None-Match header-field filterer.
     * @param file the requested file having once the passed etags associated with
     * @param etags etags from the if-match or if-none-match header field
     * @return the etag of the resource if it was not found within the list of etags
     */
    default Optional<String> anyEtagMatches(File file, List<String> etags) {
        String hashCode = generateHashCode(file);

        boolean someEtagMatched = etags.stream()
                .anyMatch(requestedHashCode -> hashCode != null && Objects.equals(requestedHashCode, hashCode));

        return someEtagMatched ? Optional.empty() : Optional.of(hashCode);
    }
}
