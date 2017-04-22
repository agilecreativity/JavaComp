package org.javacomp.parser;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;

/** Environment for using Javac parser. */
public class ParserContext {
  private final Context javacContext;
  private final JavacFileManager javacFileManager;

  public ParserContext() {
    javacContext = new Context();
    javacFileManager = new JavacFileManager(javacContext, true /* register */, UTF_8);
  }

  /**
   * Parses the content of a Java file.
   *
   * @param filename the filename of the Java file
   * @param content the content of the Java file
   */
  public JCCompilationUnit parse(String filename, CharSequence content) {
    SourceFileObject sourceFileObject = new SourceFileObject(filename);
    Log javacLog = Log.instance(javacContext);
    javacLog.useSource(sourceFileObject);

    // Create a parser and start parsing.
    JavacParser parser =
        ParserFactory.instance(javacContext)
            .newParser(
                content, true /* keepDocComments */, true /* keepEndPos */, true /* keepLineMap */);
    return parser.parseCompilationUnit();
  }
}