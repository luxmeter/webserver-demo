package luxmeter.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model representing a directory with all of its files
 * and subdirectories which are also of this type.
 * Useful when a client requests to see the content of a directory and its subdirectories.
 */
public final class Directory {
    private Directory parent;
    private List<Path> files = new ArrayList<>();
    private List<Directory> subDirectories = new ArrayList<>();
    private Path path;

    private Directory() {

    }

    /**
     * @param absoluteDirPath system path to the directory
     * @return {@link} a {@link Directory} object which can be used to list all files of the given path.
     */
    public static @Nonnull Directory listFiles(@Nonnull Path absoluteDirPath) {
        return listFiles(null, absoluteDirPath);
    }

    private static @Nonnull Directory listFiles(@Nullable Directory parent, @Nonnull Path absoluteDirPath) {
        Directory directory = new Directory();
        directory.parent = parent;
        directory.path = absoluteDirPath;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(absoluteDirPath)) {
            for (Path p : stream) {
                if (p.toFile().isFile()) {
                    directory.files.add(p);
                } else {
                    // TODO should exclude symlinks to prevent endless recursion
                    directory.subDirectories.add(listFiles(directory, p));
                }
            }
        }
        // why bother the caller with the exception handling when it can't recover form it anyway?
        catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to list files from %s", absoluteDirPath), e);
        }
        return directory;
    }

    public Directory getParent() {
        return parent;
    }

    public List<Path> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public List<Directory> getSubDirectories() {
        return Collections.unmodifiableList(subDirectories);
    }

    /**
     * @param rootDir The root directory; if none is given, the absolute path is shown.
     * @return List of all files and directories relatively to given root directory.
     */
    public String toString(@Nullable Path rootDir) {
        // StringBuilder is not required since the compiler uses it under the hood
        String output = files.stream()
                .map(p -> rootDir != null ? rootDir.relativize(p) : p)
                .map(Path::toString)
                .collect(Collectors.joining("\n"));
        if (!output.isEmpty()) {
            output += "\n";
        }
        output += subDirectories.stream()
                .map(directory -> directory.toString(rootDir))
                .collect(Collectors.joining());
        return output;
    }

    /**
     * @return the absolute system path of this directory
     */
    public String toString() {
        return this.path.toString();
    }
}
