package com.aitu.filesystem.visitor;

import com.aitu.filesystem.element.Directory;
import com.aitu.filesystem.element.File;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Operation 2: collects the full paths of all files that match a criterion. */
public final class SearchVisitor implements FileSystemVisitor {

    private static final String PATH_SEPARATOR = "/";

    private final Predicate<File> criterion;
    private final Deque<String> currentPath = new ArrayDeque<>();
    private final List<String> matchingPaths = new ArrayList<>();

    public SearchVisitor(Predicate<File> criterion) {
        this.criterion = Objects.requireNonNull(criterion, "Criterion must not be null");
    }

    public static SearchVisitor byExtension(String extension) {
        return new SearchVisitor(file -> file.getName().endsWith(extension));
    }

    @Override
    public void visitFile(File file) {
        if (criterion.test(file)) {
            matchingPaths.add(pathOf(file));
        }
    }

    @Override
    public void visitDirectory(Directory directory) {
        currentPath.addLast(directory.getName());
        directory.acceptChildren(this);
        currentPath.removeLast();
    }

    public List<String> getMatchingPaths() {
        return Collections.unmodifiableList(matchingPaths);
    }

    private String pathOf(File file) {
        return String.join(PATH_SEPARATOR, currentPath) + PATH_SEPARATOR + file.getName();
    }
}
