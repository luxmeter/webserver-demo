package luxmeter.model;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Model representing a directory with all of its files
 * and subdirectories which are also of this type.
 * Useful when a client requests to see the content of a directory and its subdirectories.
 */
public final class Directory {
    private final List<Path> files = new ArrayList<>();
    private final List<Directory> subDirectories = new ArrayList<>();
    private Directory parent;
    private Path path;

    private Directory() {

    }

    /**
     * @param absoluteDirPath system path to the directory
     * @return {@link} a {@link Directory} object which can be used to list all files of the given path.
     */
    public static @Nonnull
    Directory listFiles(@Nonnull Path absoluteDirPath) {
        return listFiles(null, absoluteDirPath);
    }

    private static @Nonnull
    Directory listFiles(Directory parent, @Nonnull Path absoluteDirPath) {
        Directory directory = new Directory();
        directory.parent = parent;
        directory.path = absoluteDirPath;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(absoluteDirPath)) {
            for (Path p : stream) {
                if (p.toFile().isFile()) {
                    directory.files.add(p);
                }
                else if (!Files.isSymbolicLink(p)) { // prevents endless recursion
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

    /**
     * @return List of all files and directories relatively to given root directory.
     * @see #getFiles(boolean)
     */
    public List<Path> getFiles() {
        return getFiles(false);
    }

    /**
     * @param recursively true if all files within subdirectories should be listed too
     * @return List of all files and directories (recursively) relatively to given root directory.
     */
    public List<Path> getFiles(boolean recursively) {
        Path root = getRoot(this);
        List<Path> result = files.stream().map(f -> root.relativize(f)).collect(Collectors.toList());
        if (recursively) {
            List<Path> relativeFiles = result;
            result = subDirectories.stream()
                    .flatMap(d -> d.getFiles(true).stream())
                    .collect(() -> relativeFiles, List::add, List::addAll);
        }
        return Collections.unmodifiableList(result);
    }

    public List<Directory> getSubDirectories() {
        return Collections.unmodifiableList(subDirectories);
    }

    /**
     * @return the absolute system path of this directory
     */
    public String toString() {
        return this.path.toString();
    }

    private static Path getRoot(Directory dir) {
        if (dir.parent == null) {
            return dir.path;
        }
        return getRoot(dir.parent);
    }
}
