# File System Visitor

Java 17 implementation of the **Visitor** behavioral design pattern
(Software Design Patterns, Assignment 6, Option A: File System Visitor).

A small file system (`File`, `Directory`) is traversed by interchangeable
operations (visitors). New operations are added without changing the elements.

## Structure

| Package | Content |
| --- | --- |
| `element` | `FileSystemElement` (interface), `File`, `Directory` |
| `visitor` | `FileSystemVisitor` (interface), `SizeCalculatorVisitor`, `SearchVisitor`, `TreePrinterVisitor` |
| root | `DemoApplication` (client) |

## Double dispatch

1. The client calls `element.accept(visitor)` (the first dispatch picks the element class).
2. `File.accept` calls `visitor.visitFile(this)`, `Directory.accept` calls
   `visitor.visitDirectory(this)` (the second dispatch picks the visitor method).

No `instanceof` checks are used anywhere.

## Run

```bash
mvn test
mvn compile exec:java -Dexec.mainClass=com.aitu.filesystem.DemoApplication
```

or run `DemoApplication` from IntelliJ IDEA (JDK 17).

## Notes

- A visitor keeps its result in a field, so create a new visitor for each run.
- `Directory.acceptChildren(visitor)` passes a visitor to every child, so visitors do not repeat the loop.
- `Directory.getChildren()` returns a read-only list.
- Create one visitor per thread. Several visitors can visit the same structure in parallel
  as long as nobody modifies it (build the structure first, then visit).
