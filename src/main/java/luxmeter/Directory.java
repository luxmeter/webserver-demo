package luxmeter;

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

class Directory {
    private Directory parent;
    private List<Path> files = new ArrayList<>();
    private List<Directory> subDirectories = new ArrayList<>();
    private Path path;

    private Directory() {

    }

    public static @Nonnull Directory listFiles(@Nonnull Path absoluteDirPath) {
        return listFiles(null, absoluteDirPath);
    }

    public static @Nonnull Directory listFiles(@Nullable Directory parent, @Nonnull Path absoluteDirPath) {
        Directory directory = new Directory();
        directory.parent = parent;
        directory.path = absoluteDirPath;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(absoluteDirPath)) {
            for (Path p : stream) {
                if (p.toFile().isFile()) {
                    directory.files.add(p);
                } else {
                    directory.subDirectories.add(listFiles(directory, p));
                }
            }
        }
        // why bother the caller with the exception handling when it can't recover form it anyway?
        catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to list files from {}", absoluteDirPath), e);
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

    public String toString(@Nullable Path rootDir) {
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

    public String toString() {
        return this.path.toString();
    }
}
