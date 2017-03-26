package luxmeter;

import javax.annotation.Nonnull;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Util {
    public static final int NO_BODY_CONTENT = -1;

    public static @Nonnull Path getAbsoluteSystemPath(Path rootDir, @Nonnull URI uri) {
        // the url could also look like http://localhost:8080 instead of http://localhost:8080/
        String relativePath = uri.getPath();
        if (uri.getPath().length() > 0) {
            relativePath = uri.getPath().substring(1); // get rid of the leading '/'
        }
        return rootDir.resolve(relativePath);
    }

    // TODO it is not necessary to generate the hash again if the file didn't change since the last time we send it
    // --> add caching of meta data (last modified date can be retrieved from the file system)
    public static String generateHashCode(File file) {
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
            for (int data = in.read(array); data != -1; data = in.read()) {
                // we better read a single byte since this will crop off all other left-handed bits
                digest.update(array);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }

        String hashCode = null;
        if (digest != null) {
            hashCode = DatatypeConverter.printHexBinary(digest.digest());
        }
        return hashCode;
    }

}
