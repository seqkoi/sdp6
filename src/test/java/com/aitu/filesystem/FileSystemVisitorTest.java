package com.aitu.filesystem;

import com.aitu.filesystem.element.Directory;
import com.aitu.filesystem.element.File;
import com.aitu.filesystem.visitor.SearchVisitor;
import com.aitu.filesystem.visitor.SizeCalculatorVisitor;
import com.aitu.filesystem.visitor.TreePrinterVisitor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileSystemVisitorTest {

    private static final int CONCURRENCY_ROUNDS = 200;
    private static final int THREAD_COUNT = 4;

    private static Directory sampleStructure() {
        Directory archive = new Directory("archive").add(new File("old.pdf", 100));
        Directory docs = new Directory("docs")
                .add(new File("report.pdf", 200))
                .add(new File("notes.txt", 50))
                .add(archive);
        return new Directory("root")
                .add(new File("readme.txt", 10))
                .add(docs);
    }

    private static long sizeOf(Directory directory) {
        SizeCalculatorVisitor visitor = new SizeCalculatorVisitor();
        directory.accept(visitor);
        return visitor.getTotalSizeBytes();
    }

    private static String treeOf(Directory directory) {
        TreePrinterVisitor visitor = new TreePrinterVisitor();
        directory.accept(visitor);
        return visitor.getOutput();
    }

    private static List<String> pdfPathsOf(Directory directory) {
        SearchVisitor visitor = SearchVisitor.byExtension(".pdf");
        directory.accept(visitor);
        return visitor.getMatchingPaths();
    }

    // ---- SizeCalculatorVisitor ----

    @Test
    void sizeOfSingleFileIsItsOwnSize() {
        SizeCalculatorVisitor visitor = new SizeCalculatorVisitor();
        new File("a.txt", 42).accept(visitor);
        assertEquals(42, visitor.getTotalSizeBytes());
    }

    @Test
    void sizeOfEmptyDirectoryIsZero() {
        assertEquals(0, sizeOf(new Directory("empty")));
    }

    @Test
    void sizeIncludesNestedDirectories() {
        assertEquals(360, sizeOf(sampleStructure()));
    }

    @Test
    void sizeCanStartFromSubDirectory() {
        Directory root = sampleStructure();
        Directory docs = (Directory) root.getChildren().get(1);
        assertEquals(350, sizeOf(docs));
    }

    // ---- SearchVisitor ----

    @Test
    void searchByExtensionReturnsFullPaths() {
        SearchVisitor visitor = SearchVisitor.byExtension(".pdf");
        sampleStructure().accept(visitor);
        assertEquals(List.of("root/docs/report.pdf", "root/docs/archive/old.pdf"),
                visitor.getMatchingPaths());
    }

    @Test
    void searchWithoutMatchReturnsEmptyList() {
        SearchVisitor visitor = SearchVisitor.byExtension(".java");
        sampleStructure().accept(visitor);
        assertEquals(List.of(), visitor.getMatchingPaths());
    }

    @Test
    void searchAcceptsAnyPredicate() {
        SearchVisitor visitor = new SearchVisitor(file -> file.getSizeBytes() < 60);
        sampleStructure().accept(visitor);
        assertEquals(List.of("root/readme.txt", "root/docs/notes.txt"),
                visitor.getMatchingPaths());
    }

    @Test
    void searchResultCannotBeModifiedByCaller() {
        SearchVisitor visitor = SearchVisitor.byExtension(".pdf");
        sampleStructure().accept(visitor);
        assertThrows(UnsupportedOperationException.class,
                () -> visitor.getMatchingPaths().add("hacked"));
    }

    // ---- TreePrinterVisitor (third visitor) ----

    @Test
    void treePrinterIndentsByDepth() {
        TreePrinterVisitor visitor = new TreePrinterVisitor();
        sampleStructure().accept(visitor);
        String expected = String.join("\n",
                "+ root/",
                "  - readme.txt (10 B)",
                "  + docs/",
                "    - report.pdf (200 B)",
                "    - notes.txt (50 B)",
                "    + archive/",
                "      - old.pdf (100 B)");
        assertEquals(expected, visitor.getOutput());
    }

    // ---- Two visitors on the same structure ----

    @Test
    void differentVisitorsGiveDifferentResultsOnSameStructure() {
        Directory root = sampleStructure();
        SizeCalculatorVisitor size = new SizeCalculatorVisitor();
        SearchVisitor search = SearchVisitor.byExtension(".txt");
        root.accept(size);
        root.accept(search);
        assertEquals(360, size.getTotalSizeBytes());
        assertEquals(2, search.getMatchingPaths().size());
    }

    // ---- Concurrent use: one visitor per thread, one shared read-only structure ----

    @Test
    void visitorsCanRunConcurrentlyOnSameStructure() throws Exception {
        Directory root = sampleStructure();
        long expectedSize = sizeOf(root);
        String expectedTree = treeOf(root);
        List<String> expectedPdfs = pdfPathsOf(root);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            for (int round = 0; round < CONCURRENCY_ROUNDS; round++) {
                Future<Long> size = executor.submit(() -> sizeOf(root));
                Future<String> tree = executor.submit(() -> treeOf(root));
                Future<List<String>> pdfs = executor.submit(() -> pdfPathsOf(root));
                assertEquals(expectedSize, size.get().longValue());
                assertEquals(expectedTree, tree.get());
                assertEquals(expectedPdfs, pdfs.get());
            }
        } finally {
            executor.shutdown();
        }
    }

    // ---- Validation (fail fast) ----

    @Test
    void negativeFileSizeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new File("a.txt", -1));
    }

    @Test
    void blankFileNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new File(" ", 1));
    }

    @Test
    void blankDirectoryNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Directory(""));
    }

    @Test
    void nullChildIsRejected() {
        assertThrows(NullPointerException.class, () -> new Directory("d").add(null));
    }

    @Test
    void nullSearchCriterionIsRejected() {
        assertThrows(NullPointerException.class, () -> new SearchVisitor(null));
    }

    @Test
    void childrenListIsReadOnly() {
        Directory directory = new Directory("d");
        assertThrows(UnsupportedOperationException.class,
                () -> directory.getChildren().add(new File("x", 1)));
    }
}
