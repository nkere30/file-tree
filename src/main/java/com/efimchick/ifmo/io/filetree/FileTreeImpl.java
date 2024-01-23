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
                return  buildTree(path, result);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    private Optional<String> buildTree(Path path, StringBuilder result) throws IOException {
        long totalSize = 0;

        List<Path> contentList = Files.list(path)
                .sorted(Comparator.comparing(Path::toString, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        int contentCount = 0;
        for (Path content: contentList) {
            boolean isLast = contentCount == contentList.size() - 1;
            contentCount++;
            Optional<String> contents = tree(content);
            if (contents.isPresent()) {
                String treeStr = contents.get();
                if (contentCount < contentList.size()) {
                    treeStr = treeStr.replaceAll("\n", "\n│  ");
                    result.append("├─ ").append(treeStr);
                } else {
                    treeStr = treeStr.replaceAll("\n", "\n  ");
                    result.append("└─ ").append(treeStr);
                }
                totalSize += getTotalSize(content);
            }
            if(!isLast) result.append("\n");
        }
        return Optional.of(path.getFileName() + " " + totalSize + " bytes\n" +  result.toString());
    }

    private long getTotalSize(Path path) throws IOException {
        return Files.walk(path)
                .filter(p -> Files.isRegularFile(p))
                .mapToLong(p -> {
                    try {
                        return Files.size(p);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .sum();
    }
}
