package com.aitu.filesystem.visitor;

import com.aitu.filesystem.element.Directory;
import com.aitu.filesystem.element.File;

/**
 * Visitor interface: one visit method for each concrete element type.
 * A new operation is a new implementation of this interface.
 */
public interface FileSystemVisitor {

    void visitFile(File file);

    void visitDirectory(Directory directory);
}
