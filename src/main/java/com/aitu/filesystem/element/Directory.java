package com.aitu.filesystem.element;

import com.aitu.filesystem.visitor.FileSystemVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Concrete element: a directory that contains files and other directories. */
public final class Directory implements FileSystemElement {

    private final String name;
    private final List<FileSystemElement> children = new ArrayList<>();

    public Directory(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Directory name must not be blank");
        }
        this.name = name;
    }

    public Directory add(FileSystemElement child) {
        children.add(Objects.requireNonNull(child, "Child must not be null"));
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    /** Read-only view: visitors can traverse the children but cannot change them. */
    public List<FileSystemElement> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /** Passes the visitor to every child in insertion order, so visitors do not repeat the loop. */
    public void acceptChildren(FileSystemVisitor visitor) {
        for (FileSystemElement child : children) {
            child.accept(visitor);
        }
    }

    /** Double dispatch, step 2: the element tells the visitor which type it is. */
    @Override
    public void accept(FileSystemVisitor visitor) {
        visitor.visitDirectory(this);
    }
}
