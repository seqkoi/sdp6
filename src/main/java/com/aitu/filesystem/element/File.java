package com.aitu.filesystem.element;

import com.aitu.filesystem.visitor.FileSystemVisitor;

/** Concrete element: a file with a name and a size. */
public final class File implements FileSystemElement {

    private final String name;
    private final long sizeBytes;

    public File(String name, long sizeBytes) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("File name must not be blank");
        }
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("File size must not be negative");
        }
        this.name = name;
        this.sizeBytes = sizeBytes;
    }

    @Override
    public String getName() {
        return name;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    /** Double dispatch, step 2: the element tells the visitor which type it is. */
    @Override
    public void accept(FileSystemVisitor visitor) {
        visitor.visitFile(this);
    }
}
