package org.eclipse.jdt.internal.compiler.parser;
import java.io.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.BindingIds;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.parser.diagnose.DiagnoseParser;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;
public class Parser
        implements
            BindingIds,
            ParserBasicInformation,
            TerminalTokens,
            CompilerModifiers,
            OperatorIds,
            TypeIds {
    protected ProblemReporter         problemReporter;
    public int                        firstToken;                                                            // handle for multiple parsing goals
    public int                        lastAct;                                                               //handle for multiple parsing goals
    protected ReferenceContext        referenceContext;
    public int                        currentToken;
    private int                       synchronizedBlockSourceStart;
    //error recovery management
    protected int                     lastCheckPoint;
    protected RecoveredElement        currentElement;
    public static boolean             VERBOSE_RECOVERY            = false;
    protected boolean                 restartRecovery;
    protected int                     listLength;                                                            // for recovering some incomplete list (interfaces, throws or parameters)
    protected boolean                 hasError;
    protected boolean                 hasReportedError;
    public static boolean             fineErrorDiagnose           = true;                                    //TODO remove the static modifier when new diagnose is ready
    public boolean                    reportSyntaxErrorIsRequired = true;
    public boolean                    reportOnlyOneSyntaxError    = false;
    protected int                     recoveredStaticInitializerStart;
    protected int                     lastIgnoredToken, nextIgnoredToken;
    protected int                     lastErrorEndPosition;
    protected boolean                 ignoreNextOpeningBrace;
    // assert is 1.4 feature only
    protected long                    sourceLevel;
    //internal data for the automat 
    protected final static int        StackIncrement              = 255;
    protected int                     stateStackTop;
    protected int[]                   stack                       = new int[StackIncrement];
    //scanner token 
    public Scanner                    scanner;
    //ast stack
    final static int                  AstStackIncrement           = 100;
    protected int                     astPtr;
    protected AstNode[]               astStack                    = new AstNode[AstStackIncrement];
    protected int                     astLengthPtr;
    protected int[]                   astLengthStack;
    public CompilationUnitDeclaration compilationUnit;                                                       /*the result from parse()*/
    AstNode[]                         noAstNodes                  = new AstNode[AstStackIncrement];
    //expression stack
    final static int                  ExpressionStackIncrement    = 100;
    protected int                     expressionPtr;
    protected Expression[]            expressionStack             = new Expression[ExpressionStackIncrement];
    protected int                     expressionLengthPtr;
    protected int[]                   expressionLengthStack;
    Expression[]                      noExpressions               = new Expression[ExpressionStackIncrement];
    //identifiers stacks 
    protected int                     identifierPtr;
    protected char[][]                identifierStack;
    protected int                     identifierLengthPtr;
    protected int[]                   identifierLengthStack;
    protected long[]                  identifierPositionStack;
    //positions , dimensions , .... (what ever is int) ..... stack
    protected int                     intPtr;
    protected int[]                   intStack;
    protected int                     endPosition;                                                           //accurate only when used ! (the start position is pushed into intStack while the end the current one)
    protected int                     endStatementPosition;
    protected int                     lParenPos, rParenPos;                                                  //accurate only when used !
    //modifiers dimensions nestedType etc.......
    protected boolean                 optimizeStringLiterals      = true;
    protected int                     modifiers;
    protected int                     modifiersSourceStart;
    protected int                     nestedType, dimensions;
    protected int[]                   nestedMethod;                                                          //the ptr is nestedType
    protected int[]                   realBlockStack;
    protected int                     realBlockPtr;
    protected boolean                 diet                        = false;                                   //tells the scanner to jump over some parts of the code/expressions like method bodies
    protected int                     dietInt                     = 0;                                       // if > 0 force the none-diet-parsing mode (even if diet if requested) [field parsing with anonymous inner classes...]
    protected int[]                   variablesCounter;
    public void foo() {
        byte rhs[] = {0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 3, 4, 0, 1, 2, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 5, 1, 2, 1, 2, 2, 2, 1, 1, 2, 2, 2, 4, 1, 1,
                1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 3, 2, 2, 1, 3,
                1, 3, 1, 2, 1, 1, 1, 3, 0, 3, 1, 1, 1, 1, 1, 1, 1, 4, 1, 3, 3,
                7, 0, 0, 0, 0, 0, 2, 1, 1, 1, 2, 2, 4, 4, 5, 4, 4, 2, 1, 2, 3,
                3, 1, 3, 3, 1, 3, 1, 4, 0, 2, 1, 2, 2, 4, 1, 1, 2, 5, 5, 7, 7,
                7, 7, 2, 2, 3, 2, 2, 3, 1, 2, 1, 2, 1, 1, 2, 2, 1, 1, 1, 1, 1,
                3, 3, 4, 1, 3, 4, 0, 1, 2, 1, 1, 1, 1, 2, 3, 4, 0, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3,
                3, 2, 1, 1, 1, 1, 1, 1, 1, 5, 7, 7, 6, 2, 3, 3, 4, 1, 2, 2, 1,
                2, 3, 2, 5, 5, 7, 9, 9, 1, 1, 1, 1, 3, 3, 5, 2, 3, 2, 3, 3, 3,
                5, 1, 3, 4, 1, 2, 5, 2, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 3, 3,
                3, 3, 1, 1, 5, 6, 8, 7, 2, 0, 2, 0, 1, 3, 3, 3, 3, 4, 3, 4, 1,
                2, 3, 2, 1, 1, 2, 2, 3, 3, 4, 6, 6, 4, 4, 4, 1, 1, 1, 1, 2, 2,
                0, 1, 1, 3, 3, 1, 3, 3, 1, 3, 3, 1, 6, 6, 5, 0, 0, 1, 3, 3, 3,
                1, 3, 3, 1, 3, 3, 3, 1, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 1, 3, 1,
                3, 1, 3, 1, 3, 1, 5, 1, 1, 3, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 2, 0, 1,
                0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 2, 0, 0, 1, 0, 1, 0, 1,
                0, 1};
    }
}