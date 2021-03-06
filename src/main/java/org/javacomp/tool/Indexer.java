package org.javacomp.tool;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.javacomp.file.SimpleFileManager;
import org.javacomp.options.IndexOptions;
import org.javacomp.project.Project;
import org.javacomp.storage.IndexStore;

/**
 * Creates index files for specified source code.
 *
 * <p>Usage: Indexer <root path> <output file> <ignored paths...>
 */
public class Indexer {

  private final Path rootPath;
  private final SimpleFileManager fileManager;
  private final Project project;

  public Indexer(String rootPath, List<String> ignoredPaths) {
    this.rootPath = Paths.get(rootPath);
    this.fileManager = new SimpleFileManager(this.rootPath, ignoredPaths);
    this.project =
        new Project(
            fileManager, this.rootPath.toUri(), IndexOptions.PUBLIC_READONLY_BUILDER.build());
  }

  public void run(String indexFile, List<String> dependIndexFiles, boolean withJdk) {
    project.initialize();
    for (String dependIndexFile : dependIndexFiles) {
      project.loadTypeIndexFile(dependIndexFile);
    }
    if (withJdk) {
      project.loadJdkModule();
    }
    new IndexStore().writeModuleToFile(project.getModule(), Paths.get(indexFile));
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Usage: Indexer <root path> <output file> [options]");
      System.out.println("  Options:");
      System.out.println("    --depend|-d <index files...>");
      System.out.println("    --ignore|-i <ignored paths...>]");
      System.out.println("    --no-jdk      Do not load JDK module.");
      return;
    }
    List<String> ignorePaths = new ArrayList<>();
    List<String> dependIndexPaths = new ArrayList<>();
    List<String> currentList = null;
    boolean withJdk = true;
    for (int i = 2; i < args.length; i++) {
      String arg = args[i];
      if ("--depend".equals(arg) || "-d".equals(arg)) {
        currentList = dependIndexPaths;
      } else if ("--ignore".equals(arg) || "-i".equals(arg)) {
        currentList = ignorePaths;
      } else if ("--no-jdk".equals(arg)) {
        withJdk = false;
      } else if (currentList == null) {
        System.err.println("-i or -d must be specified before " + arg);
        System.exit(1);
      } else {
        currentList.add(arg);
      }
    }

    new Indexer(args[0], ignorePaths).run(args[1], dependIndexPaths, withJdk);
  }
}
