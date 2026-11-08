package com.aitu.filesystem.element;

import com.aitu.filesystem.visitor.FileSystemVisitor;

/**
 * Element interface of the Visitor pattern.
 * Every node of the file system accepts a visitor.
 */
public interface FileSystemElement {

    String getName();

    void accept(FileSystemVisitor visitor);
}
