package com.aitu.filesystem.visitor;

import com.aitu.filesystem.element.Directory;
import com.aitu.filesystem.element.File;

import java.util.ArrayList;
import java.util.List;

/**
 * Operation 3: builds an indented text view of the structure.
 * Added after the first two visitors without changing File or Directory.
 */
public final class TreePrinterVisitor implements FileSystemVisitor {

    private static final String INDENT = "  ";

    private final List<String> lines = new ArrayList<>();
    private int depth;

    @Override
    public void visitFile(File file) {
        lines.add(indentation() + "- " + file.getName() + " (" + file.getSizeBytes() + " B)");
    }

    @Override
    public void visitDirectory(Directory directory) {
        lines.add(indentation() + "+ " + directory.getName() + "/");
        depth++;
        directory.acceptChildren(this);
        depth--;
    }

    public String getOutput() {
        return String.join("\n", lines);
    }

    private String indentation() {
        return INDENT.repeat(depth);
    }
}
