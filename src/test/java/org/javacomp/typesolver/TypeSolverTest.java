package org.javacomp.typesolver;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.javacomp.model.ClassSymbol;
import org.javacomp.model.FileIndex;
import org.javacomp.model.GlobalIndex;
import org.javacomp.model.MethodSymbol;
import org.javacomp.model.SolvedType;
import org.javacomp.model.Symbol;
import org.javacomp.model.SymbolIndex;
import org.javacomp.model.TypeReference;
import org.javacomp.parser.AstScanner;
import org.javacomp.parser.SourceFileObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TypeSolverTest {
  private static final String TEST_DATA_DIR = "src/test/java/org/javacomp/typesolver/testdata/";
  private static final String[] TEST_FILES = {
    "BaseInterface.java",
    "TestClass.java",
    "other/BaseClass.java",
    "other/Shadow.java",
    "ondemand/OnDemand.java",
    "ondemand/Shadow.java",
  };
  private static final String TEST_DATA_PACKAGE = "org.javacomp.typesolver.testdata";
  private static final String TEST_DATA_OTHER_PACKAGE = TEST_DATA_PACKAGE + ".other";
  private static final String TEST_DATA_ONDEMAND_PACKAGE = TEST_DATA_PACKAGE + ".ondemand";
  private static final String TEST_CLASS_FULL_NAME = TEST_DATA_PACKAGE + ".TestClass";
  private static final String TEST_CLASS_FACTORY_FULL_NAME =
      TEST_CLASS_FULL_NAME + ".TestClassFactory";
  private static final String BASE_INTERFACE_FULL_NAME = TEST_DATA_PACKAGE + ".BaseInterface";
  private static final String BASE_INTERFACE_FACTORY_FULL_NAME =
      BASE_INTERFACE_FULL_NAME + ".BaseInterfaceFactory";
  private static final String BASE_CLASS_FULL_NAME = TEST_DATA_OTHER_PACKAGE + ".BaseClass";
  private static final String OTHER_SHADOW_CLASS_FULL_NAME = TEST_DATA_OTHER_PACKAGE + ".Shadow";
  private static final String BASE_INNER_CLASS_FULL_NAME = BASE_CLASS_FULL_NAME + ".BaseInnerClass";
  private static final String ON_DEMAND_CLASS_FULL_NAME = TEST_DATA_ONDEMAND_PACKAGE + ".OnDemand";
  private static final Joiner QUALIFIER_JOINER = Joiner.on(".");

  private Log javacLog;
  private GlobalIndex globalIndex;
  private Context javacContext;
  private final TypeSolver typeSolver = new TypeSolver();

  @Before
  public void setUpTestIndex() throws Exception {
    javacContext = new Context();
    javacLog = Log.instance(javacContext);
    JavacFileManager fileManager = new JavacFileManager(javacContext, true /* register */, UTF_8);
    globalIndex = new GlobalIndex();
    for (String filename : TEST_FILES) {
      String inputFilePath = TEST_DATA_DIR + filename;
      globalIndex.addOrReplaceFileIndex(parseTestFile(inputFilePath));
    }
  }

  private FileIndex parseTestFile(String filePath) throws Exception {
    String fileContent = new String(Files.readAllBytes(Paths.get(filePath)), UTF_8);

    // If source file not set, parser will throw IllegalArgumentException when errors occur.
    SourceFileObject sourceFileObject = new SourceFileObject(filePath);
    javacLog.useSource(sourceFileObject);

    JavacParser parser =
        ParserFactory.instance(javacContext)
            .newParser(
                fileContent,
                true /* keepDocComments */,
                true /* keepEndPos */,
                true /* keepLineMap */);
    JCCompilationUnit compilationUnit = parser.parseCompilationUnit();
    return new AstScanner().startScan(compilationUnit, filePath);
  }

  @Test
  public void solveBaseInterfaceInTheSamePackage() {
    ClassSymbol testClass = (ClassSymbol) lookupSymbol(TEST_CLASS_FULL_NAME);
    TypeReference baseInterfaceReference = testClass.getInterfaces().get(0);
    Optional<SolvedType> solvedType =
        typeSolver.solve(baseInterfaceReference, globalIndex, testClass);
    assertThat(solvedType).isPresent();
    assertThat(solvedType.get().getClassSymbol()).isSameAs(lookupSymbol(BASE_INTERFACE_FULL_NAME));
  }

  @Test
  public void solveClassDefinedInSuperInterface() {
    ClassSymbol testClass = (ClassSymbol) lookupSymbol(TEST_CLASS_FACTORY_FULL_NAME);
    TypeReference baseInterfaceReference = testClass.getInterfaces().get(0);
    Optional<SolvedType> solvedType =
        typeSolver.solve(baseInterfaceReference, globalIndex, testClass);
    assertThat(solvedType).isPresent();
    assertThat(solvedType.get().getClassSymbol())
        .isSameAs(lookupSymbol(BASE_INTERFACE_FACTORY_FULL_NAME));
  }

  @Test
  public void solveInnerClass() {
    SolvedType baseInterface = solveMethodReturnType(TEST_CLASS_FULL_NAME + ".newFactory");
    assertThat(baseInterface.getClassSymbol()).isSameAs(lookupSymbol(TEST_CLASS_FACTORY_FULL_NAME));
  }

  @Test
  public void solveBaseClassInOtherPackage() {
    ClassSymbol testClass = (ClassSymbol) lookupSymbol(TEST_CLASS_FULL_NAME);
    TypeReference baseClassReference = testClass.getSuperClass().get();
    Optional<SolvedType> solvedType = typeSolver.solve(baseClassReference, globalIndex, testClass);
    assertThat(solvedType).isPresent();
    assertThat(solvedType.get().getClassSymbol()).isSameAs(lookupSymbol(BASE_CLASS_FULL_NAME));
  }

  @Test
  public void solveInnerClassInBaseClassFromOtherPackage() {
    ClassSymbol testClass = (ClassSymbol) lookupSymbol(TEST_CLASS_FACTORY_FULL_NAME);
    TypeReference baseInnerClassReference = testClass.getSuperClass().get();
    Optional<SolvedType> solvedType =
        typeSolver.solve(baseInnerClassReference, globalIndex, testClass);
    assertThat(solvedType).isPresent();
    assertThat(solvedType.get().getClassSymbol())
        .isSameAs(lookupSymbol(BASE_INNER_CLASS_FULL_NAME));
  }

  @Test
  public void solveOnDemandClassImport() {
    SolvedType onDemandClass = solveMethodReturnType(TEST_CLASS_FULL_NAME + ".getOnDemand");
    assertThat(onDemandClass.getClassSymbol()).isSameAs(lookupSymbol(ON_DEMAND_CLASS_FULL_NAME));
  }

  @Test
  public void onDemandPackageClassShouldBeShadowed() {
    // both other and ondemand package define Shadow class. Shadow from other package should be used
    // since it's explicitly imported.
    SolvedType shadowClass = solveMethodReturnType(TEST_CLASS_FULL_NAME + ".getShadow");
    assertThat(shadowClass.getClassSymbol()).isSameAs(lookupSymbol(OTHER_SHADOW_CLASS_FULL_NAME));
  }

  private SolvedType solveMethodReturnType(String qualifiedMethodName) {
    MethodSymbol method = (MethodSymbol) lookupSymbol(qualifiedMethodName);
    assertThat(method).isNotNull();
    MethodSymbol.Overload methodOverload = method.getOverloads().get(0);
    TypeReference methodReturnType = methodOverload.getReturnType();
    Optional<SolvedType> solvedType =
        typeSolver.solve(
            methodReturnType, globalIndex, methodOverload.getMethodIndex().getParentClass());
    assertThat(solvedType).isPresent();
    return solvedType.get();
  }

  private Symbol lookupSymbol(String qualifiedName) {
    String[] qualifiers = qualifiedName.split("\\.");
    SymbolIndex currentIndex = globalIndex;
    Symbol symbol = null;
    List<String> currentQualifiers = new ArrayList<>();
    for (String qualifier : qualifiers) {
      currentQualifiers.add(qualifier);
      Collection<Symbol> symbols = currentIndex.getAllSymbols().get(qualifier);
      assertWithMessage(QUALIFIER_JOINER.join(currentQualifiers)).that(symbols).isNotEmpty();
      symbol = Iterables.getFirst(symbols, null);
      currentIndex = symbol.getChildIndex();
    }
    return symbol;
  }
}
