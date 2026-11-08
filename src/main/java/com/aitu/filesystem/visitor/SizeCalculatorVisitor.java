package com.aitu.filesystem.visitor;

import com.aitu.filesystem.element.Directory;
import com.aitu.filesystem.element.File;

/** Operation 1: calculates the total size in bytes of everything it visits. */
public final class SizeCalculatorVisitor implements FileSystemVisitor {

    private long totalSizeBytes;

    @Override
    public void visitFile(File file) {
        totalSizeBytes += file.getSizeBytes();
    }

    @Override
    public void visitDirectory(Directory directory) {
        directory.acceptChildren(this);
    }

    public long getTotalSizeBytes() {
        return totalSizeBytes;
    }
}
