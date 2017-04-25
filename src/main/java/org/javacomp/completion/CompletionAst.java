package org.javacomp.completion;

import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import org.javacomp.logging.JLogger;
import org.javacomp.typesolver.ExpressionSolver;

/** Retrieve completion information from the AST. */
class CompletionAst {
  private static final JLogger logger = JLogger.createForEnclosingClass();

  private final ExpressionSolver expressionSolver;

  CompletionAst(ExpressionSolver expressionSolver) {
    this.expressionSolver = expressionSolver;
  }

  public CompletionAction getCompletionAction(JCCompilationUnit compilationUnit, int position) {
    CompletionAstScanner completionAstScanner =
        new CompletionAstScanner(compilationUnit.endPositions, position);
    logger.fine("Starting completion scan, position: %s", position);
    TreePath treePath = completionAstScanner.scan(compilationUnit, null);
    logger.fine("TreePath for completion: %s", TreePathFormatter.formatTreePath(treePath));
    if (treePath.getLeaf() instanceof MemberSelectTree) {
      return new CompleteMemberAction(treePath, expressionSolver);
    }
    return new CompleteEntityAction();
  }

  /** An AST scanner that returns the last meaningful sub tree for completion. */
  private static class CompletionAstScanner extends TreePathScanner<TreePath, Void> {
    private final EndPosTable endPosTable;
    private final int position;

    private CompletionAstScanner(EndPosTable endPosTable, int position) {
      this.endPosTable = endPosTable;
      this.position = position;
    }

    @Override
    public TreePath scan(Tree tree, Void unused) {
      if (tree == null) {
        return null;
      }

      JCTree jcTree = (JCTree) tree;
      logger.fine(
          "Completion scanner: visiting node: %s, start: %s, end: %s.",
          tree.accept(new TreePathFormatter.TreeFormattingVisitor(), null),
          jcTree.getStartPosition(),
          jcTree.getEndPosition(endPosTable));
      if (jcTree.getStartPosition() >= position || jcTree.getEndPosition(endPosTable) < position) {
        return null;
      }
      TreePath currentPath = new TreePath(getCurrentPath(), tree);

      TreePath ret = super.scan(tree, null);
      if (ret != null) {
        return ret;
      }

      return currentPath;
    }

    @Override
    public TreePath reduce(TreePath r1, TreePath r2) {
      if (r1 != null) {
        return r1;
      }
      return r2;
    }
  }
}
