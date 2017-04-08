package luxmeter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.bind.DatatypeConverter;

import com.sun.net.httpserver.Headers;

/**
 * Provides convenient methods like the access of header-fields for which different spelling variations might exist.
 */
public final class Util {
    public static final int NO_BODY_CONTENT = -1;
    public static final int NO_RESONSE_RETURNED_YET = -1;

    /**
     * @return transforms the requestedUrl with the help of rootDir to an absolute system path.
     */
    @Nonnull
    public static Path getAbsoluteSystemPath(@Nonnull Path rootDir, @Nonnull URI requestedUrl) {
        // the url could also look like http://localhost:8080 instead of http://localhost:8080/
        String relativePath = requestedUrl.getPath();
        if (requestedUrl.getPath().length() > 0) {
            relativePath = requestedUrl.getPath().substring(1); // get rid of the leading '/'
        }
        return rootDir.resolve(relativePath);
    }

    /**
     * @return Generates a MD5 hash of the file's content. Returns null if file does not exist.
     */
    // TODO it is not necessary to generate the hash again if the file didn't change since the last time we send it
    // --> add caching of meta data (last modified date can be retrieved from the file system)
    public static String generateHashCode(@Nonnull File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        // why do stream twice for a single resource?
        // --> we can't generate the hashCode adhoc when we stream the data to the client
        // since the hash code needs to be send first in the header response
        // --> to avoid this stream we could read all the content of the resource first into memory
        // but no one knows how big this data is.
        // so better stream twice if necessary
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            byte[] array = new byte[4];
            digest = MessageDigest.getInstance("MD5");
            for (int data = in.read(array); data != -1; data = in.read(array)) {
                // we better read a single byte since this will crop off all other left-handed bits
                digest.update(array);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException(String.format("Unable to generate hash code for %s: ", file), e);
        }

        String hashCode = DatatypeConverter.printHexBinary(digest.digest());
        return hashCode;
    }

    /**
     * With this function it is neither needed to try different spelling variants of the key
     * nor to null-check the return value since it will be always a collection returned.
     * <br/>
     * If it is wanted to manipulate the field values, the Headers#add should be used.
     *
     * @param headers header-fields from request or response
     * @see Headers#add(String, String)
     * @return <b>unmodifiable</b> list of the header-field values
     */
    public static List<String> getHeaderFieldValues(@Nonnull Headers headers, @Nonnull String key) {
        List<String> values = headers.get(key);
        if (values == null) {
            return headers.keySet().stream()
                    .filter(k -> k.equalsIgnoreCase(key))
                    .findFirst()
                    .map(headers::get)
                    .map(Collections::unmodifiableList)
                    .orElse(Collections.emptyList());
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * @param existingFile real <b>existing</b> file
     * @param preferredZone zone to convert the local date time to.
     * @return
     */
    @Nonnull
    public static ZonedDateTime getLastModifiedDate(@Nonnull File existingFile, @Nonnull ZoneId preferredZone) {
        return ZonedDateTime
                .ofInstant(Instant.ofEpochMilli(existingFile.lastModified()), ZonedDateTime.now().getZone())
                .withZoneSameLocal(preferredZone);
    }
}
