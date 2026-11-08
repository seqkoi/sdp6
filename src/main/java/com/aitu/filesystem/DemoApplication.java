package com.aitu.filesystem;

import com.aitu.filesystem.element.Directory;
import com.aitu.filesystem.element.File;
import com.aitu.filesystem.visitor.SearchVisitor;
import com.aitu.filesystem.visitor.SizeCalculatorVisitor;
import com.aitu.filesystem.visitor.TreePrinterVisitor;

/** Client: builds one structure and runs several visitors on it. */
public final class DemoApplication {

    private static final long SMALL_FILE_LIMIT_BYTES = 3_000;

    public static void main(String[] args) {
        Directory root = buildSampleStructure();
        Directory docs = (Directory) root.getChildren().get(1);

        TreePrinterVisitor treePrinter = new TreePrinterVisitor();
        root.accept(treePrinter);
        System.out.println("=== Tree (TreePrinterVisitor) ===");
        System.out.println(treePrinter.getOutput());

        System.out.println();
        System.out.println("=== SizeCalculatorVisitor ===");
        System.out.println("Total size of root: " + totalSize(root) + " B");
        System.out.println("Total size of docs: " + totalSize(docs) + " B");

        System.out.println();
        System.out.println("=== SearchVisitor ===");
        print("Files ending with .pdf:", SearchVisitor.byExtension(".pdf"), root);
        print("Files ending with .java:", SearchVisitor.byExtension(".java"), root);
        print("Files smaller than " + SMALL_FILE_LIMIT_BYTES + " B:",
                new SearchVisitor(file -> file.getSizeBytes() < SMALL_FILE_LIMIT_BYTES), root);
    }

    private static Directory buildSampleStructure() {
        Directory archive = new Directory("archive")
                .add(new File("old-report.pdf", 180_000));
        Directory docs = new Directory("docs")
                .add(new File("report.pdf", 250_000))
                .add(new File("notes.txt", 3_400))
                .add(archive);
        Directory src = new Directory("src")
                .add(new File("Main.java", 4_200))
                .add(new File("Utils.java", 2_800));
        Directory images = new Directory("images")
                .add(new File("logo.png", 52_000));
        return new Directory("root")
                .add(new File("readme.txt", 1_200))
                .add(docs)
                .add(src)
                .add(images);
    }

    private static long totalSize(Directory directory) {
        SizeCalculatorVisitor sizeCalculator = new SizeCalculatorVisitor();
        directory.accept(sizeCalculator);
        return sizeCalculator.getTotalSizeBytes();
    }

    private static void print(String title, SearchVisitor search, Directory root) {
        root.accept(search);
        System.out.println(title);
        search.getMatchingPaths().forEach(path -> System.out.println("  " + path));
    }
}
