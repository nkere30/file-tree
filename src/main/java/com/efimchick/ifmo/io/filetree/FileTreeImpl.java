package com.efimchick.ifmo.io.filetree;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileTreeImpl implements FileTree {

    @Override
    public Optional<String> tree(Path path) {
        if (path == null || !Files.exists(path)) {
            return Optional.empty();
        }


        try {
            if (Files.isRegularFile(path)) {
                return Optional.of(path.getFileName() + " " + Files.size(path) + " bytes");
            } else if (Files.isDirectory(path)) {
                StringBuilder result = new StringBuilder();
                buildTree(path, result, "");
                return Optional.of(result.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    private void buildTree(Path path, StringBuilder result, String prefix) throws IOException {
        long totalSize = Files.walk(path)
                .filter(p -> Files.isRegularFile(p))
                .mapToLong(p -> {
                    try {
                        return Files.size(p);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .sum();

        List<Path> contentList = Files.list(path)
                .sorted(Comparator.comparing(Path::toString, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        result.append(prefix)
                .append(path.getFileName())
                .append(" ")
                .append(totalSize)
                .append(" bytes")
                .append("\n");

        int contentCount = 0;
        for (Path content: contentList) {
            boolean isLast = contentCount == contentList.size() - 1;
            contentCount++;
            result.append(prefix)
                    .append(isLast ? "└─ " : "├─ ");
            if (Files.isDirectory(content)) {
                buildTree(content, result, prefix + (isLast ? "   " : "│  "));
            } else {
                result.append(content.getFileName()).append(" ").append(Files.size(content)).append(" bytes").append("\n");
            }
        }

    }
}
